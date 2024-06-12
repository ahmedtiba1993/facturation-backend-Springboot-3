package com.facturation.service;

import com.facturation.exception.EntityNotFoundException;
import com.facturation.exception.InvalidEntityException;
import com.facturation.model.Devis;
import com.facturation.model.Facture;
import com.facturation.repository.DevisRepository;
import com.facturation.repository.FactureRepository;
import com.itextpdf.text.DocumentException;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final FactureRepository factureRepository;
    private final FactureService factureService;
    private final DevisRepository devisRepository;
    private final DevisService devisService;


    @Async
    public void sendEmailFacture(Long factureId) throws MessagingException, IOException, DocumentException {

        Facture facture = factureRepository.findById(factureId).get();
        if (facture == null) {
            throw new EntityNotFoundException("FACTURE_NOT_FOUND");
        }

        if (facture.getClient().getEmail() == null) {
            throw new InvalidEntityException("EMAIL_EMPTY");
        }

        if (facture != null) {
            String templateName = EmailTemplateName.FACTURE_TEMPLATE.getName();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());

            Map<String, Object> properties = new HashMap<>();
            properties.put("username", facture.getClient().getNomCommercial());
            properties.put("dateFacture", facture.getDateFacture());
            properties.put("montantTTC", facture.getMontantTTC() + " TND");
            properties.put("refFacture", facture.getReference());

            Context context = new Context();
            context.setVariables(properties);

            helper.setTo(facture.getClient().getEmail());
            helper.setSubject("Facture " + facture.getReference());

            String template = templateEngine.process(templateName, context);

            helper.setText(template, true);

            var pdfFile = factureService.generatePdf(List.of(factureId)).getBody();
            if (pdfFile != null) {
                DataSource dataSource = new ByteArrayDataSource(pdfFile.getInputStream(), "application/pdf");
                helper.addAttachment("Facture " + facture.getReference(), dataSource);
            }

            mailSender.send(mimeMessage);
        }
    }

    @Async
    public void sendEmailDevis(Long devisId) throws MessagingException, IOException, DocumentException {

        Devis devis = devisRepository.findById(devisId).get();
        if (devis == null) {
            throw new EntityNotFoundException("DEVIS_NOT_FOUND");
        }

        if (devis.getClient().getEmail() == null) {
            throw new InvalidEntityException("EMAIL_EMPTY");
        }

        if (devis != null) {
            String templateName = EmailTemplateName.DEVIS_TEMPLATE.getName();
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MULTIPART_MODE_MIXED, UTF_8.name());

            Map<String, Object> properties = new HashMap<>();
            properties.put("username", devis.getClient().getNomCommercial());
            properties.put("dateDevis", devis.getDateDevis());
            properties.put("montantTTC", devis.getMontantTTC() + " TND");
            properties.put("refDevis", devis.getReference());

            Context context = new Context();
            context.setVariables(properties);

            helper.setTo(devis.getClient().getEmail());
            helper.setSubject("Devis " + devis.getReference());

            String template = templateEngine.process(templateName, context);

            helper.setText(template, true);

            var pdfFile = devisService.generatePdf(List.of(devis.getId())).getBody();
            if (pdfFile != null) {
                DataSource dataSource = new ByteArrayDataSource(pdfFile.getInputStream(), "application/pdf");
                helper.addAttachment("Devis " + devis.getReference(), dataSource);
            }

            mailSender.send(mimeMessage);
        }

    }
}
