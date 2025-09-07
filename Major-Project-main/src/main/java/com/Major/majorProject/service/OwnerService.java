package com.Major.majorProject.service;

import com.Major.majorProject.dto.CafeAdditionDto;
import com.Major.majorProject.dto.OwnerRegistrationDto;
import com.Major.majorProject.dto.PCDto;
import com.Major.majorProject.dto.SlotDetails;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.entity.User;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerService {

    private final CafeOwnerRepository cafeOwnerRepository;
    private final CafeRepository cafeRepository;
    private final PCRepository pcRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserBookingRepository userBookingRepository;
    private final UserRepository userRepository;

    public OwnerService(CafeOwnerRepository cor, PasswordEncoder pe, CafeRepository cr, PCRepository pcr, UserBookingRepository ubr, UserRepository ur) {
        this.cafeOwnerRepository = cor;
        this.passwordEncoder = pe;
        this.cafeRepository = cr;
        this.pcRepository = pcr;
        this.userBookingRepository = ubr;
        this.userRepository = ur;
    }

    public void ownerRegistration(OwnerRegistrationDto ord) {
        CafeOwner owner = new CafeOwner();
        owner.setName(ord.getName());
        owner.setEmail(ord.getEmail());
        owner.setPhone(ord.getPhone());
        owner.setPassword(passwordEncoder.encode(ord.getPassword()));
        cafeOwnerRepository.save(owner);
    }

    public void cafeAddition(CafeAdditionDto cad) {
        String ownerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        CafeOwner owner = cafeOwnerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Cafe cafe = new Cafe();
        cafe.setName(cad.getName());
        cafe.setAddress(cad.getAddress());
        cafe.setOpenTime(cad.getOpenTime());
        cafe.setCloseTime(cad.getCloseTime());
        cafe.setHourlyRate(cad.getHourlyRate());
        cafe.setOwner(owner);
        cafeRepository.save(cafe);
    }

    public List<CafeAdditionDto> getAllCafeOfOwner() {
        String ownerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        CafeOwner owner = cafeOwnerRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (owner.getCafes() == null) {
            return Collections.emptyList();
        }

        return owner.getCafes().stream().map(cafe -> {
            CafeAdditionDto dto = new CafeAdditionDto();
            dto.setId(cafe.getId());
            dto.setName(cafe.getName());
            dto.setAddress(cafe.getAddress());
            dto.setOpenTime(cafe.getOpenTime());
            dto.setCloseTime(cafe.getCloseTime());
            dto.setHourlyRate(cafe.getHourlyRate());
            // Calculate available PCs
            long availableCount = cafe.getPcs().stream()
                    .map(pc -> getPcAvailability(pc.getId()))
                    .filter(status -> status.equals("Available"))
                    .count();
            dto.setAvailablePcs((int) availableCount);
            return dto;
        }).collect(Collectors.toList());
    }

    public void addPC(long cafeId, PCDto pcdto) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        PC pc = new PC();
        pc.setSeatNumber(pcdto.getSeatNumber());
        pc.setConfiguration(pcdto.getConfiguration());
        pc.setAvailable("Available");
        pc.setCafe(cafe);
        pcRepository.save(pc);
    }

    public List<PCDto> getAllPcOfCafe(long cafeId) {
        List<PC> pcs = pcRepository.findByCafeId(cafeId);
        return pcs.stream().map(this::mapPcToPcDto).collect(Collectors.toList());
    }

    public List<CafeAdditionDto> getAllCafes() {
        return cafeRepository.findAll().stream().map(cafe -> {
            CafeAdditionDto dto = new CafeAdditionDto();
            dto.setId(cafe.getId());
            dto.setName(cafe.getName());
            dto.setAddress(cafe.getAddress());
            dto.setOpenTime(cafe.getOpenTime());
            dto.setCloseTime(cafe.getCloseTime());
            dto.setHourlyRate(cafe.getHourlyRate());
            long availableCount = cafe.getPcs().stream()
                    .map(pc -> getPcAvailability(pc.getId()))
                    .filter(status -> status.equals("Available"))
                    .count();
            dto.setAvailablePcs((int) availableCount);
            return dto;
        }).collect(Collectors.toList());
    }

    public CafeAdditionDto getCafeDtoById(long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found with ID: " + cafeId));
        CafeAdditionDto dto = new CafeAdditionDto();
        dto.setId(cafe.getId());
        dto.setName(cafe.getName());
        dto.setAddress(cafe.getAddress());
        dto.setOpenTime(cafe.getOpenTime());
        dto.setCloseTime(cafe.getCloseTime());
        dto.setHourlyRate(cafe.getHourlyRate());
        return dto;
    }

    public String getPcAvailability(Long pcId) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found"));
        Cafe cafe = pc.getCafe();
        List<UserBooking> bookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());

        int totalSlots = cafe.getCloseTime().getHour() - cafe.getOpenTime().getHour();
        if (bookings.isEmpty()) {
            return "Available";
        } else if (bookings.size() >= totalSlots) {
            return "Full";
        } else {
            return "Busy";
        }
    }

    public List<LocalTime> getAvailableSlotsForPC(long pcId) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found with id: " + pcId));
        Cafe cafe = pc.getCafe();
        LocalTime openTime = cafe.getOpenTime();
        LocalTime closeTime = cafe.getCloseTime();

        List<UserBooking> todaysBookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());
        List<LocalTime> bookedStartTimes = todaysBookings.stream().map(UserBooking::getStartTime).toList();

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime potentialStartTime = openTime;
        LocalTime now = LocalTime.now();

        while (potentialStartTime.isBefore(closeTime)) {
            LocalTime potentialEndTime = potentialStartTime.plusHours(1);

            if (!bookedStartTimes.contains(potentialStartTime) &&
                    !potentialStartTime.isBefore(now) &&
                    !potentialEndTime.isAfter(closeTime)) {
                availableSlots.add(potentialStartTime);
            }

            potentialStartTime = potentialStartTime.plusHours(1);
        }
        return availableSlots;
    }


    public void bookSlot(long pcId, LocalTime startTime) {
        PC pc = pcRepository.findById(pcId)
                .orElseThrow(() -> new RuntimeException("PC not found for booking."));

        UserBooking booking = new UserBooking();
        booking.setPc(pc);
        //booking.setUser(user);
        booking.setBookingDate(LocalDate.now());
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusHours(1));

        userBookingRepository.save(booking);
    }

    private PCDto mapPcToPcDto(PC pc) {
        PCDto pcDto = new PCDto();
        pcDto.setId(pc.getId());
        pcDto.setSeatNumber(pc.getSeatNumber());
        pcDto.setConfiguration(pc.getConfiguration());
        pcDto.setAvailable(getPcAvailability(pc.getId()));
        pcDto.setCafeId(pc.getCafe().getId());
        pcDto.setCafeName(pc.getCafe().getName());
        return pcDto;
    }

    public PCDto findPCById(long pcId) {
        PC pc = pcRepository.findById(pcId)
                .orElseThrow(() -> new RuntimeException("PC not found with ID: " + pcId));
        return mapPcToPcDto(pc);
    }

    public List<SlotDetails> getAllSlotsOfPc(long pcId) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found"));
        Cafe cafe = pc.getCafe();
        LocalTime openTime = cafe.getOpenTime();
        LocalTime closeTime = cafe.getCloseTime();

        List<UserBooking> bookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());

        List<SlotDetails> slots = new ArrayList<>();
        LocalTime currentTime = openTime;

        while (currentTime.isBefore(closeTime)) {
            SlotDetails slot = new SlotDetails();
            slot.setStartTime(currentTime.toString());
            LocalTime endTime = currentTime.plusHours(1);
            slot.setEndTime(endTime.toString());
            slot.setStatus("open");

            for (UserBooking booking : bookings) {
                if (currentTime.isBefore(booking.getEndTime()) && endTime.isAfter(booking.getStartTime())) {
                    slot.setStatus("closed");
                    break;
                }
            }
            slots.add(slot);
            currentTime = endTime;
        }
        return slots;
    }


}