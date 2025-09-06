package com.Major.majorProject.configuration;
import com.Major.majorProject.entity.CafeOwner;
import com.Major.majorProject.repository.CafeOwnerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CafeOwnerDetailsService implements UserDetailsService {

    private final CafeOwnerRepository ownerRepository;

    public CafeOwnerDetailsService(CafeOwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        CafeOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with email: " + email));
        return new CafeOwnerDetails(owner);
    }
}

