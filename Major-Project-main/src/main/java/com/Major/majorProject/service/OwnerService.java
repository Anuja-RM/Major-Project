package com.Major.majorProject.service;

import com.Major.majorProject.dto.*;
import com.Major.majorProject.entity.*;
import com.Major.majorProject.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final SlotRepository slotRepository;

    public OwnerService(CafeOwnerRepository cor, PasswordEncoder pe, CafeRepository cr, PCRepository pcr, UserBookingRepository ubr, UserRepository ur, SlotRepository slotRepository) {
        this.cafeOwnerRepository = cor;
        this.passwordEncoder = pe;
        this.cafeRepository = cr;
        this.pcRepository = pcr;
        this.userBookingRepository = ubr;
        this.userRepository = ur;
        this.slotRepository = slotRepository;
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
        List<LocalTime> allSlots = slotRepository.findByPcId(pcId)
                .stream()
                .map(Slot::getStartTime)
                .collect(Collectors.toList());

        List<UserBooking> bookings = userBookingRepository.findByPcIdAndBookingDate(pcId, LocalDate.now());
        List<LocalTime> bookedSlots = bookings.stream()
                .map(UserBooking::getStartTime)
                .collect(Collectors.toList());

        return allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    public void bookSlot(long pcId, LocalTime startTime) {
        PC pc = pcRepository.findById(pcId).orElseThrow(() -> new RuntimeException("PC not found"));
        UserBooking booking = new UserBooking();
        booking.setPc(pc);
        booking.setBookingDate(LocalDate.now());
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusHours(1));
        // booking.setUser(currentUser);
        userBookingRepository.save(booking);
    }


    public PCDto findPCById(long pcId) {
        PC pc = pcRepository.findById(pcId)
                .orElseThrow(() -> new RuntimeException("PC not found with ID: " + pcId));
        return mapPcToPcDto(pc);
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
            slot.setStartTime(currentTime);
            LocalTime endTime = currentTime.plusHours(1);
            slot.setEndTime(endTime);
            slot.setStatus("open");
            slot.setCafeId(cafe.getId());

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

    public void addPC(long cafeId, PCDto pcdto) {
        Cafe cafe = cafeRepository.findById(cafeId)
                .orElseThrow(() -> new RuntimeException("Cafe not found"));
        PC pc = new PC();
        pc.setSeatNumber(pcdto.getSeatNumber());
        pc.setConfiguration(pcdto.getConfiguration());
        pc.setAvailable("Available");
        pc.setCafe(cafe);
        PC savedPC = pcRepository.save(pc);
        generateSlotsForPC(savedPC, cafe.getOpenTime(), cafe.getCloseTime());
    }

    private void generateSlotsForPC(PC pc, LocalTime openTime, LocalTime closeTime) {
        LocalTime currentTime = openTime;
        while (currentTime.isBefore(closeTime)) {
            Slot slot = new Slot();
            slot.setPc(pc);
            slot.setStartTime(currentTime);
            slot.setEndTime(currentTime.plusHours(1));
            slot.setBooked(false);
            slotRepository.save(slot);
            currentTime = currentTime.plusHours(1);
        }
    }

    public PC getPCById(long pcId) {
        return pcRepository.findById(pcId).orElse(null);
    }

    public List<SlotDetails> getSlotsForPC(long pcId) {
        List<Slot> savedSlots = slotRepository.findByPcId(pcId);

        if (savedSlots.isEmpty()) {
            return Collections.emptyList();
        }

        PC pc = getPCById(pcId);
        long cafeId = (pc != null && pc.getCafe() != null) ? pc.getCafe().getId() : 0;

        return savedSlots.stream()
                .map(slot -> {
                    SlotDetails details = new SlotDetails();
                    details.setId(slot.getId());
                    details.setStartTime(slot.getStartTime());
                    details.setEndTime(slot.getEndTime());
                    details.setCafeId(cafeId);

                    if (slot.isBooked()) {
                        details.setStatus("booked");
                    } else if (slot.getEndTime().isBefore(LocalTime.now())) {
                        details.setStatus("past");
                    } else {
                        details.setStatus("open");
                    }
                    return details;
                })
                .sorted(Comparator.comparing(SlotDetails::getStartTime))
                .collect(Collectors.toList());
    }

    @Transactional
    public void addSlots(SlotDto slotDto) {
        PC pc = pcRepository.findById(slotDto.getPcId())
                .orElseThrow(() -> new RuntimeException("PC not found with id: " + slotDto.getPcId()));

        if (pc.getSlots() == null) {
            pc.setSlots(new ArrayList<>());
        }

        for (LocalTime startTime : slotDto.getStartTime()) {
            Slot slot = new Slot();
            slot.setPc(pc); // Set the PC on the slot
            slot.setStartTime(startTime);
            slot.setEndTime(startTime.plusHours(1)); // Assuming 1-hour slots
            slot.setBooked(false);

            pc.getSlots().add(slot); // Add the new slot to the PC's list

            slotRepository.save(slot);
        }
    }

}