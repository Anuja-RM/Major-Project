package com.Major.majorProject.controller;

import com.Major.majorProject.dto.CafeAdditionDto;
import com.Major.majorProject.dto.PCDto;
import com.Major.majorProject.dto.SlotDetails;
import com.Major.majorProject.dto.SlotDto;
import com.Major.majorProject.entity.PC;
import com.Major.majorProject.service.OwnerService;
import org.springframework.stereotype.Controller; // Changed from RestController
import org.springframework.ui.Model; // Added Model
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller // Changed from RestController
@RequestMapping("/owner")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService os){
        this.ownerService = os;
    }

    @GetMapping("/")
    public String ownerDashboard(Model model){
        List<CafeAdditionDto> cafes = ownerService.getAllCafeOfOwner();
        model.addAttribute("cafes", cafes);
        return "owner/ownerDashboard";
    }

    @GetMapping("/addCafe")
    public String showAddCafeForm(Model model){
        model.addAttribute("cafeAdditionDto", new CafeAdditionDto());
        return "owner/addCafe";
    }

    @PostMapping("/addCafe")
    public String cafeAddition(@ModelAttribute("cafeAdditionDto") CafeAdditionDto cad ){
        ownerService.cafeAddition(cad);
        return "redirect:/owner/";
    }

    @GetMapping("/cafes")
    public String getAllCafeOfOwner(Model model){
        List<CafeAdditionDto> cafes = ownerService.getAllCafeOfOwner();
        model.addAttribute("cafes", cafes);
        return "owner/cafeList";
    }

    @GetMapping("/addSeat/{cafeId}")
    public String showAddPCForm(@PathVariable("cafeId") long cafeId, Model model){
        model.addAttribute("cafeId", cafeId);
        model.addAttribute("pcDto", new PCDto());
        return "owner/addPc";
    }

    @PostMapping("/addSeat/{cafeId}")
    public String addPC(@PathVariable("cafeId") long cafeId, @ModelAttribute("pcDto") PCDto pcd){
        ownerService.addPC(cafeId, pcd);
        return "redirect:/owner/PCs/" + cafeId;
    }

    @GetMapping("/PCs/{cafeId}")
    public String getAllPcOfCafe(@PathVariable("cafeId") long cafeId, Model model){
        List<PCDto> pcs = ownerService.getAllPcOfCafe(cafeId);
        model.addAttribute("pcs", pcs);
        model.addAttribute("cafeId", cafeId);
        return "owner/pcList";
    }

    @GetMapping("/slots/{pcId}")
    public String showSlotList(@PathVariable("pcId") long pcId, Model model) {
        List<SlotDetails> slots = ownerService.getSlotsForPC(pcId);
        PC pc = ownerService.getPCById(pcId);

        model.addAttribute("slots", slots);
        model.addAttribute("pcId", pcId);
        if (pc != null && pc.getCafe() != null) {
            model.addAttribute("cafeId", pc.getCafe().getId());
        } else {
            model.addAttribute("cafeId", 0);
        }
        return "owner/slotList";
    }

    @GetMapping("/addSlots/{pcId}")
    public String showAddSlotsForm(@PathVariable("pcId") long pcId, Model model){
        model.addAttribute("slotDto", new SlotDto());
        model.addAttribute("pcId", pcId);
        return "owner/addSlots";
    }

    @PostMapping("/addSlots/{pcId}")
    public String addSlots(@PathVariable("pcId") long pcId, @ModelAttribute("slotDto") SlotDto slotDto){
        slotDto.setPcId(pcId);
        ownerService.addSlots(slotDto);
        return "redirect:/owner/slots/" + pcId;
    }
}