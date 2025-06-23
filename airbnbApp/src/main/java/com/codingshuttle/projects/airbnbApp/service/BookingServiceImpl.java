package com.codingshuttle.projects.airbnbApp.service;

import com.codingshuttle.projects.airbnbApp.dto.BookingDto;
import com.codingshuttle.projects.airbnbApp.dto.BookingRequest;
import com.codingshuttle.projects.airbnbApp.dto.GuestDto;
import com.codingshuttle.projects.airbnbApp.dto.HotelReportDto;
import com.codingshuttle.projects.airbnbApp.entity.*;
import com.codingshuttle.projects.airbnbApp.enums.BookingStatus;
import com.codingshuttle.projects.airbnbApp.exception.ResourceNotFoundException;
import com.codingshuttle.projects.airbnbApp.exception.UnAuthorizedException;
import com.codingshuttle.projects.airbnbApp.repository.*;
import com.codingshuttle.projects.airbnbApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;


import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final CheckOutService checkOutService;
    private final PricingService pricingService;
    @Value("${frontend.url}")
    private String url;
    @Override
    @Transactional
    public BookingDto intialiseBooking(BookingRequest bookingRequest) {
        log.info("Initializing booking for hotel :{},room:{},date:{}-{}",bookingRequest.getHotelId(),bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate());
        Hotel hotel=hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id:"+bookingRequest.getHotelId()));
        Room room=roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()->new ResourceNotFoundException("Room not found with Id:"+bookingRequest.getRoomId()));
        List<Inventory> inventoryList=inventoryRepository.findAndLockAvailableInventory(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());
        long daysCount= ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate())+1;
        if(inventoryList.size()!=daysCount){
            throw new IllegalStateException("Room is not available anymore");
        }
        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());
        BigDecimal priceForOneRoom=pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice=priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));
        Booking booking=Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);

    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
       log.info("adding guests for booking id:{}",bookingId);
       Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found with id:"+bookingId));
       User user=getCurrentUser();
       if(!user.equals(booking.getUser())){
           throw new UnAuthorizedException("Booking does not belong to this user");
       }
       if(hasBookingExpired(booking)){
           throw new IllegalStateException("booking expired");
       }
       if(booking.getBookingStatus()!=BookingStatus.RESERVED){
           throw new IllegalStateException("Booking not in correct state");
       }
       for(GuestDto guestDto:guestDtoList){
           Guest guest=modelMapper.map(guestDto,Guest.class);
           guest.setUser(user);
           guest=guestRepository.save(guest);
           booking.getGuests().add(guest);
       }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        booking=bookingRepository.save(booking);
       return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    public String initiatePayments(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user");
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("booking expired");
        }
        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
       String sessionUrl= checkOutService.getCheckOutSession(booking,url,url);
        return sessionUrl;
    }

    @Override
    public void capturePayment(Event event) {
        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) return;
            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(() -> new ResourceNotFoundException("booking not found"));
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

        }else{
            log.warn("unhandled event type");
        }
    }

    @Override
    public void cancelBooking(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user");
        }
        if(hasBookingExpired(booking)){
            throw new IllegalStateException("booking expired");
        }
        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("only confirmed bookings can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams= RefundCreateParams.builder()
                                                .setPaymentIntent(session.getPaymentIntent())

                                                .build();
            Refund.create(refundParams);
        }catch(StripeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found"));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnAuthorizedException("Booking does not belong to this user");
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotel(Long hotelId) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("The hotel is not found"));
        User user=getCurrentUser();
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the ownerof hotel");
        List<Booking> bookings=bookingRepository.findByHotel(hotel);
        return bookings.stream().map(booking->modelMapper.map(booking,BookingDto.class)).toList();

    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("The hotel is not found"));
        User user=getCurrentUser();
        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the ownerof hotel");
        LocalDateTime startDateTime=startDate.atStartOfDay();
        LocalDateTime endDateTime=endDate.atTime(LocalTime.MAX);
        List<Booking> bookings=bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);
        Long totalConfirmedBookings=bookings.stream().filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED).count();
       BigDecimal totalRevenueOfConfirmedBookings=bookings.stream().filter(booking->booking.getBookingStatus()==BookingStatus.CONFIRMED)
               .map(booking->booking.getAmount())
               .reduce(BigDecimal.ZERO,BigDecimal::add);
       BigDecimal averageRevenue=totalConfirmedBookings==0?BigDecimal.ZERO:totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);
        return new HotelReportDto(totalConfirmedBookings,averageRevenue,totalRevenueOfConfirmedBookings);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user=getCurrentUser();
        return bookingRepository.findByUser(user).stream().map(element->modelMapper.map(element,BookingDto.class)).toList();
    }

    public boolean hasBookingExpired(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
    public User getCurrentUser(){
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
