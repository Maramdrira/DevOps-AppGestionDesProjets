package tn.esprit.backend;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import tn.esprit.backend.entity.Entreprise;
import tn.esprit.backend.service.IEntrepriseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EntrepriseServiceTest {

    @Autowired
    private IEntrepriseService entrepriseService;

    private static Long createdId;

    @Test
    @Order(1)
    @DisplayName("Test 1: Add a new Entreprise")
    void testAddEntreprise() {
        Entreprise e = Entreprise.builder()
                .nom("Test Company")
                .adresse("Tunis")
                .build();

        Entreprise saved = entrepriseService.addEntreprise(e);

        assertNotNull(saved, "Saved entreprise should not be null");
        assertNotNull(saved.getId(), "ID should be generated");
        assertEquals("Test Company", saved.getNom());
        assertEquals("Tunis", saved.getAdresse());

        createdId = saved.getId();
        System.out.println("Test 1 passed — created id=" + createdId);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Retrieve all Entreprises")
    void testGetAllEntreprises() {
        List<Entreprise> list = entrepriseService.getAllEntreprises();

        assertNotNull(list);
        assertFalse(list.isEmpty(), "List should contain at least one entreprise");
        System.out.println("Test 2 passed — found " + list.size() + " entreprise(s)");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Get Entreprise by ID")
    void testGetEntrepriseById() {
        assertNotNull(createdId, "createdId must be set by Test 1");

        Entreprise found = entrepriseService.getEntrepriseById(createdId);

        assertNotNull(found);
        assertEquals(createdId, found.getId());
        System.out.println(" Test 3 passed — found: " + found.getNom());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Update Entreprise")
    void testUpdateEntreprise() {
        assertNotNull(createdId, "createdId must be set by Test 1");

        Entreprise e = entrepriseService.getEntrepriseById(createdId);
        e.setNom("Updated Company");
        e.setAdresse("Sfax");

        Entreprise updated = entrepriseService.updateEntreprise(e);

        assertNotNull(updated);
        assertEquals("Updated Company", updated.getNom());
        assertEquals("Sfax", updated.getAdresse());
        System.out.println(" Test 4 passed — updated name=" + updated.getNom());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Delete Entreprise")
    void testDeleteEntreprise() {
        assertNotNull(createdId, "createdId must be set by Test 1");

        entrepriseService.deleteEntreprise(createdId);

        Entreprise deleted = entrepriseService.getEntrepriseById(createdId);
        assertNull(deleted, "Entreprise should be null after deletion");
        System.out.println(" Test 5 passed — deleted id=" + createdId);
    }
}
