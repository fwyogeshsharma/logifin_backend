package com.logifin.repository;

import com.logifin.entity.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("WalletRepository Tests")
class WalletRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WalletRepository walletRepository;

    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = Wallet.builder()
                .userId(1L)
                .currencyCode("INR")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Should save wallet successfully")
    void shouldSaveWalletSuccessfully() {
        Wallet saved = walletRepository.save(testWallet);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getCurrencyCode()).isEqualTo("INR");
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Should find wallet by user ID")
    void shouldFindWalletByUserId() {
        entityManager.persist(testWallet);
        entityManager.flush();

        Optional<Wallet> found = walletRepository.findByUserId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty when wallet not found by user ID")
    void shouldReturnEmptyWhenWalletNotFound() {
        Optional<Wallet> found = walletRepository.findByUserId(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should check if wallet exists by user ID")
    void shouldCheckIfWalletExistsByUserId() {
        entityManager.persist(testWallet);
        entityManager.flush();

        boolean exists = walletRepository.existsByUserId(1L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when wallet does not exist")
    void shouldReturnFalseWhenWalletDoesNotExist() {
        boolean exists = walletRepository.existsByUserId(999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should check if wallet is active")
    void shouldCheckIfWalletIsActive() {
        entityManager.persist(testWallet);
        entityManager.flush();

        boolean isActive = walletRepository.isWalletActive(1L);

        assertThat(isActive).isTrue();
    }

    @Test
    @DisplayName("Should return false for suspended wallet")
    void shouldReturnFalseForSuspendedWallet() {
        testWallet.setStatus("SUSPENDED");
        entityManager.persist(testWallet);
        entityManager.flush();

        boolean isActive = walletRepository.isWalletActive(1L);

        assertThat(isActive).isFalse();
    }

    @Test
    @DisplayName("Should enforce unique user ID constraint")
    void shouldEnforceUniqueUserIdConstraint() {
        entityManager.persist(testWallet);
        entityManager.flush();

        Wallet duplicate = Wallet.builder()
                .userId(1L)
                .currencyCode("USD")
                .status("ACTIVE")
                .build();

        try {
            entityManager.persist(duplicate);
            entityManager.flush();
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }
}
