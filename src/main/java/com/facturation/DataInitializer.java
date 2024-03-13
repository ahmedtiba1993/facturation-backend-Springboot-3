package com.facturation;

import com.facturation.model.NumFacture;
import com.facturation.model.TimbreFiscal;
import com.facturation.model.Tva;
import com.facturation.repository.NumFactureRepository;
import com.facturation.repository.TimbreFiscalRepository;
import com.facturation.repository.TvaRepository;
import com.facturation.service.UserService;
import com.facturation.user.Role;
import com.facturation.user.User;
import com.facturation.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TvaRepository tvaRepository;

    @Autowired
    private TimbreFiscalRepository timbreFiscalRepository;

    @Autowired
    private NumFactureRepository numFactureRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /*Optional<User> user = userRepository.findByEmail("admin");
        List<Tva> tva = tvaRepository.findAll();
        List<TimbreFiscal> timbreFiscal= timbreFiscalRepository.findAll();
        if(user.isEmpty()){
            User userAdded = new User();
            userAdded.setEmail("admin");
            userAdded.setFirstname("admin");
            userAdded.setLastname("admin");
            userAdded.setRole(Role.ADMIN);
            userAdded.setFax(123456789);
            userAdded.setMobile(123456789);
            userAdded.setTel(123456789);
            userAdded.setPassword("$2a$12$e4uA1xw9wMHDglkMO07cwe8juf2.Db.ama9lU5Kadckv71UBE9cyG");
            userRepository.save(userAdded);
        }

        if(tva.size() == 0){
            Tva tva1 = new Tva();
            tva1.setCode("TVA");
            tva1.setTva(19);
            tva1.setId(1L);
            tvaRepository.save(tva1);
        }
        if(timbreFiscal.size()==0){
            TimbreFiscal timbreFiscal1 = new TimbreFiscal();
            timbreFiscal1.setCode("TIMBRE");
            timbreFiscal1.setMontant(1000);
            timbreFiscal1.setId(1L);
            timbreFiscalRepository.save(timbreFiscal1);
        }

        List<NumFacture> numFactures = numFactureRepository.findAll();
        if(numFactures.size() == 0){
            NumFacture numFacture = new NumFacture();
            numFacture.setId(1L);
            numFacture.setNumDevis(0);
            numFacture.setNumFacture(0);
            numFactureRepository.save(numFacture);
        }*/
    }
}
