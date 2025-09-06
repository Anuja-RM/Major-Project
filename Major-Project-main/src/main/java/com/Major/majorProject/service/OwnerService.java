package com.Major.majorProject.service;

import com.Major.majorProject.dto.CafeAdditionDto;
import com.Major.majorProject.dto.OwnerRegistrationDto;
import com.Major.majorProject.dto.PCDto;
import com.Major.majorProject.dto.SlotDetails;
import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.entity.UserBooking;
import com.Major.majorProject.repository.CafeOwnerRepository;
import com.Major.majorProject.repository.CafeRepository;
import com.Major.majorProject.repository.PCRepository;
import com.Major.majorProject.repository.UserBookingRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class OwnerService {

    private final CafeOwnerRepository cafeOwnerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CafeRepository cafeRepository;
    private final PCRepository pcRepository;
    private final UserBookingRepository userBookingRepository;

    public OwnerService(CafeOwnerRepository cor, PasswordEncoder pe,
                        CafeRepository cr, PCRepository pcr,
                        UserBookingRepository ubr){
        this.cafeOwnerRepository = cor;
        this.passwordEncoder = pe;
        this.cafeRepository = cr;
        this.pcRepository = pcr;
        this.userBookingRepository = ubr;
    }


    public OwnerRegistrationDto ownerRegistration(OwnerRegistrationDto ord) {
        if(cafeOwnerRepository.findByEmail(ord.getEmail()).isPresent()){
            throw new RuntimeException("This email id is already associated with other client"+ord.getEmail());
        }

        CafeOwner cafeOwner = new CafeOwner();
        cafeOwner.setName(ord.getName());
        cafeOwner.setEmail(ord.getEmail())  ;
        cafeOwner.setPhone(ord.getPhone());
        cafeOwner.setPassword(passwordEncoder.encode(ord.getPassword()));

        cafeOwnerRepository.save(cafeOwner);
        return ord;
    }

    public void cafeAddition(CafeAdditionDto cad) {
        String ownerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        CafeOwner owner = cafeOwnerRepository.findByEmail(ownerEmail).get();

        Cafe cafe = new Cafe();
        cafe.setOwner(owner);
        cafe.setName(cad.getName());
        cafe.setAddress(cad.getAddress());
        cafe.setOpenTime(cad.getOpenTime());
        cafe.setCloseTime(cad.getCloseTime());
        cafe.setHourlyRate(cad.getHourlyRate());
        cafeRepository.save(cafe);
    }

    public List<CafeAdditionDto> getAllCafeOfOwner() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        CafeOwner owner = cafeOwnerRepository.findByEmail(email).get();
        List<Cafe> cafes = cafeRepository.findAllByOwner(owner);
        List<CafeAdditionDto> dto = new ArrayList<>();
        for(Cafe cafe : cafes){
            CafeAdditionDto cad = new CafeAdditionDto();
            cad.setId(cafe.getId());
            cad.setName(cafe.getName());
            cad.setAddress(cafe.getAddress());
            cad.setOpenTime(cafe.getOpenTime());
            cad.setCloseTime(cafe.getCloseTime());
            cad.setHourlyRate(cafe.getHourlyRate());
            dto.add(cad);
        }
        return dto;
    }

    public void addPC(long cafeId, PCDto pcd) {
        Cafe cafe = cafeRepository.findById(cafeId).get();
        PC pc = new PC();
        pc.setSeatNumber(pcd.getSeatNumber());
        pc.setConfiguration(pcd.getConfiguration());
        pc.setAvailable(pcd.getAvailable());
        pc.setCafe(cafe);

        pcRepository.save(pc);
    }

    public List<PCDto> getAllPcOfCafe(long cafeId) {
        Cafe cafe = cafeRepository.findById(cafeId).get();
        List<PCDto> allPc = new ArrayList<>();
        List<PC> pcs;
        Optional<List<PC>> temp = pcRepository.findAllByCafe(cafe);
        if(temp.isPresent()){
            pcs = temp.get();
        }else{
            return allPc;
        }

        for(PC pc : pcs){
            PCDto dto = new PCDto();
            dto.setId(pc.getId());
            dto.setSeatNumber(pc.getSeatNumber());
            dto.setAvailable(pc.getAvailable());
            dto.setConfiguration(pc.getConfiguration());
            allPc.add(dto);
        }
        return allPc;
    }

    public List<SlotDetails> getAllSlotsOfPc(long pcId) {
        PC pc = pcRepository.findById(pcId).get();
        Cafe cafe = pc.getCafe();
        LocalDate date = LocalDate.now();
        List<UserBooking> bookings = userBookingRepository.findByBookingDateAndPc(date,pc);
        HashSet<Integer> hs = new HashSet<>();
        for(UserBooking ub : bookings){
            hs.add(ub.getStartTime().getHour());
        }
        int startTime = cafe.getOpenTime().getHour();
        int closeTime = cafe.getCloseTime().getHour();
        int totalSlots = closeTime - startTime;
        int start = startTime;
        List<SlotDetails> slots = new ArrayList<>();
        for(int i=1; i<=totalSlots; i++){
            SlotDetails sd = new SlotDetails();
            sd.setStartTime(start + ".00");
            sd.setEndTime(start+1 + ".00");
            sd.setStatus((hs.contains(start)) ? "CLOSED" : "OPEN");
            start++;
            slots.add(sd);
        }
        return slots;
    }
}