package com.Major.majorProject.controller;

import com.Major.majorProject.dto.CafeAdditionDto;
import com.Major.majorProject.dto.PCDto;
import com.Major.majorProject.dto.SlotDetails;
import com.Major.majorProject.service.OwnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final OwnerService ownerService;

    public UserController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @GetMapping("/findcafe")
    public String findCafes(Model model) {
        List<CafeAdditionDto> cafes = ownerService.getAllCafes();
        model.addAttribute("cafes", cafes);
        return "user/userFindCafe";
    }

    @GetMapping("/cafes/{cafeId}")
    public String cafeDetails(@PathVariable("cafeId") long cafeId, Model model) {
        CafeAdditionDto cafeDto = ownerService.getCafeDtoById(cafeId);
        List<PCDto> pcs = ownerService.getAllPcOfCafe(cafeId);

        model.addAttribute("cafe", cafeDto);
        model.addAttribute("pcs", pcs);
        model.addAttribute("reviews", List.of());
        return "user/userCafeDetails";
    }

    @PostMapping("/book/{pcId}")
    public String showBookingSlots(@PathVariable("pcId") long pcId, Model model) {
        PCDto pcDto = ownerService.findPCById(pcId);
        List<LocalTime> slots = ownerService.getAvailableSlotsForPC(pcId);
        model.addAttribute("pc", pcDto);
        model.addAttribute("availableSlots", slots);
        return "user/userBookingSlots";
    }


    @PostMapping("/confirm-booking")
    public String confirmBooking(@RequestParam("pcId") long pcId, @RequestParam("startTime") String startTime) {
        System.out.println("Confirmed booking for PC ID: " + pcId + " at slot starting: " + startTime);
        return "redirect:/user/booking-confirmation";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation() {
        return "user/userBookingConfirmation";
    }
}