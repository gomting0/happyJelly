package com.ex.service;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ex.data.VaccinationsDTO;
import com.ex.entity.VaccinationsEntity;
import com.ex.repository.VaccinationsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VaccinationsService {
    private final VaccinationsRepository vaccinationsRepository;
	private final String UPLOAD_PATH="C:\\spring\\upload\\";

   

    @Transactional
    public void saveVaccinationWithFile(VaccinationsDTO vaccinationDTO, MultipartFile file) {
       
    	String filename = profile(file);
        vaccinationDTO.setFilename(filename);

        VaccinationsEntity vaccination = convertToEntity(vaccinationDTO);
        vaccinationsRepository.save(vaccination);
    }

    public String profile(MultipartFile profile) {
        String orgname = profile.getOriginalFilename();
        String contentType = null;
        String sysname = null;
        String ext = null;
        File copy = null;
        if(profile != null && !orgname.equals("")) {
            contentType = profile.getContentType();
            if(contentType != null && contentType.split("/")[0].equals("image")) {
                sysname = UUID.randomUUID().toString().replace("-", "");
                ext = orgname.substring(orgname.lastIndexOf("."));
                sysname += ext;
                copy = new File(UPLOAD_PATH + sysname);
                try {
                    profile.transferTo(copy);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sysname;
    }

    private VaccinationsEntity convertToEntity(VaccinationsDTO dto) {
        System.out.println("VaccineType from DTO: " + dto.getVaccineType());

        VaccinationsEntity entity = VaccinationsEntity.builder()
                .dogs(dto.getDogs())
                .vaccineType(dto.getVaccineType())
                .vaccination_date(dto.getVaccination_date())
                .expiry_date(dto.getExpiry_date())
                .filename(dto.getFilename())
                .build();

        System.out.println("VaccineType in Entity: " + entity.getVaccineType());

        return entity;
    }

    public List<VaccinationsEntity> getVaccinationsByDogId(Integer dogId) {
        return vaccinationsRepository.findAll().stream()
                .filter(vaccination -> vaccination.getDogs().getDogId().equals(dogId))
                .collect(Collectors.toList());
    }
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_PATH).resolve(filename).normalize();
            org.springframework.core.io.Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + filename, ex);
        }
    }
    }


