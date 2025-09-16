package com.Major.majorProject.controller;

import com.Major.majorProject.dto.CafeAdditionDto;
import com.Major.majorProject.dto.PCDto;
import com.Major.majorProject.dto.SlotDetails;
import com.Major.majorProject.service.OwnerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping("/pcs/{pcId}/slots")
    public String showBookingSlots(@PathVariable("pcId") long pcId,
                                   @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   Model model) {
        PCDto pcDto = ownerService.findPCById(pcId);
        List<LocalTime> slots = ownerService.getAvailableSlotsForPC(pcId, date);
        model.addAttribute("pc", pcDto);
        model.addAttribute("availableSlots", slots);
        model.addAttribute("bookingDate", date);
        return "user/userBookingSlots";
    }

    @PostMapping("/pre-confirm-booking")
    public String preConfirmBooking(@RequestParam("pcId") long pcId,
                                    @RequestParam("startTime") String startTime,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        if (userDetails == null) {
            model.addAttribute("pcId", pcId);
            model.addAttribute("startTime", startTime);
            return "user/userRegistrationPrompt";
        }
        return "redirect:/user/confirm-booking?pcId=" + pcId + "&startTime=" + startTime;
    }


    @GetMapping("/confirm-booking")
    public String confirmBooking(@RequestParam("pcId") long pcId,
                                 @RequestParam("startTime") String startTime,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        ownerService.bookSlot(pcId, LocalTime.parse(startTime));
        return "redirect:/user/booking-confirmation";
    }

    @GetMapping("/booking-confirmation")
    public String bookingConfirmation() {
        return "user/userBookingConfirmation";
    }
}