package com.rest.rest.controller;

import com.example.rest.exception.ResourceNotFoundException;
import com.rest.model.rest;
import com.rest.repository.restRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api")
public class RestController {

    @Autowired
    RestRepository restRepository;

    @GetMapping("/rest")
    public List<Rest> getAllRest() {
        return restRepository.findAll();
    }

    @PostMapping("/rest")
    public Rest createRest(@Valid @RequestBody Rest rest) {
        return restRepository.save(rest);
    }

    @GetMapping("/rest/{id}")
    public Rest getRestById(@PathVariable(value = "id") Long restId) {
        return restRepository.findById(restId)
                .orElseThrow(() -> new ResourceNotFoundException("Rest", "id", restId));
    }

    @PutMapping("/rests/{id}")
    public Rest updateRest(@PathVariable(value = "id") Long restId,
                                           @Valid @RequestBody Rest restDetails) {

        Rest rest = restRepository.findById(restId)
                .orElseThrow(() -> new ResourceNotFoundException("Rest", "id", restId));

        rest.setTitle(restDetails.getTitle());
        rest.setContent(restDetails.getContent());

        Rest updatedRest = restRepository.save(rest);
        return updatedRest;
    }

    @DeleteMapping("/rests/{id}")
    public ResponseEntity<?> deleteRest(@PathVariable(value = "id") Long restId) {
        Rest rest = restRepository.findById(restId)
                .orElseThrow(() -> new ResourceNotFoundException("rest", "id", restId));

        restRepository.delete(rest);

        return ResponseEntity.ok().build();
    }

    private class RestRepository {
        public List<Rest> findAll() {
            return null;
        }

        public Rest save(Rest rest) {
            return null;
        }

        public void delete(Rest rest) {

        }

        public Optional<Object> findById(Long restId) {
            return null;
        }
    }

    private class Rest {
        public Object getTitle() {
            return null;
        }

        public Object getContent() {
            return null;
        }

        public void setTitle(Object title) {

        }
    }
}
