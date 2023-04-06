package com.facturation.service.impl;

import com.facturation.dto.TimbreFiscalDto;
import com.facturation.exception.ErrorCodes;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.TimbreFiscal;
import com.facturation.repository.TimbreFiscalRepository;
import com.facturation.service.TimbreFiscalService;
import com.facturation.validator.TimbreFiscaleValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TimbreFiscalServiceImpl implements TimbreFiscalService {

    private TimbreFiscalRepository timbreFiscalRepository;

    @Autowired
    public TimbreFiscalServiceImpl(TimbreFiscalRepository timbreFiscalRepository) {
        this.timbreFiscalRepository = timbreFiscalRepository;
    }

    @Override
    public TimbreFiscalDto ajouter(TimbreFiscalDto dto) {
        List<String> errors = TimbreFiscaleValidator.validate(dto);

        if (!errors.isEmpty()) {
            log.error("Timbre fiscale is not valid {} ",dto);
            throw new InvalidEntityException("Timbre fiscale n est pas valide", ErrorCodes.TIMBRE_FISCALE_NOT_VALID, errors);
        }

        return TimbreFiscalDto.fromEntity(timbreFiscalRepository.save(TimbreFiscalDto.toEntity(dto)));
    }

    @Override
    public TimbreFiscalDto getTimbreFiscale() {
        Optional<TimbreFiscal> timbreFiscale = timbreFiscalRepository.findTimbreFiscale("TIMBRE");
        TimbreFiscalDto timbreFiscalDto = timbreFiscale.map(TimbreFiscalDto::fromEntity).orElse(null);
        return timbreFiscalDto;
    }
}
