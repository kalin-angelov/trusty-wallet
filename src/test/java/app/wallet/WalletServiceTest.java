package app.wallet;

import app.credit.service.CreditService;
import app.exception.WalletDoNotExistException;
import app.transaction.service.TransactionService;
import app.user.model.User;
import app.wallet.model.Wallet;
import app.wallet.model.WalletStatus;
import app.wallet.model.WalletType;
import app.wallet.repository.WalletRepository;
import app.wallet.service.WalletService;
import app.web.dto.TransferRequest;
import app.web.dto.WalletsReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private CreditService creditService;

    @InjectMocks
    private WalletService walletService;

    @Test
    void givenNewUser_whenRegister_thenTreeWalletsAreCreated() {

        User user = User.builder().build();

        Wallet defaultWallet = Wallet.builder()
                .owner(user)
                .type(WalletType.DEFAULT)
                .balance(new BigDecimal(10))
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet savingWallet = Wallet.builder()
                .owner(user)
                .type(WalletType.SAVING)
                .balance(new BigDecimal(0))
                .status(WalletStatus.INACTIVE)
                .build();

        Wallet payableWallet = Wallet.builder()
                .owner(user)
                .type(WalletType.PAYABLE)
                .balance(new BigDecimal(0))
                .status(WalletStatus.INACTIVE)
                .build();

        List<Wallet> wallets = walletService.createUserWallets(user);

        assertThat(wallets).hasSize(3);
        assertEquals(wallets.get(0).getBalance(), defaultWallet.getBalance());
        assertEquals(wallets.get(0).getStatus(), defaultWallet.getStatus());
        assertEquals(wallets.get(0).getType(), defaultWallet.getType());
        assertEquals(wallets.get(1).getStatus(), savingWallet.getStatus());
        assertEquals(wallets.get(1).getBalance(), savingWallet.getBalance());
        assertEquals(wallets.get(1).getType(), savingWallet.getType());
        assertEquals(wallets.get(2).getStatus(), payableWallet.getStatus());
        assertEquals(wallets.get(2).getBalance(), payableWallet.getBalance());
        assertEquals(wallets.get(2).getType(), payableWallet.getType());
        verify(walletRepository, times(1)).saveAll(wallets);
    }

    @Test
    void givenActiveWallet_whenChangeStatus_thenTheStatusIsChangedToInactive() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(WalletStatus.ACTIVE)
                .build();

        when(walletRepository.findByIdAndOwnerId(wallet.getId(), user.getId())).thenReturn(Optional.of(wallet));

        Wallet changeWalletStatus = walletService.changeStatus(wallet.getId(), user);

        assertEquals(changeWalletStatus.getStatus(), WalletStatus.INACTIVE);
        verify(walletRepository, times(1)).save(changeWalletStatus);
    }

    @Test
    void givenInactiveWallet_whenChangeStatus_thenTheStatusIsChangedToActive() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .status(WalletStatus.INACTIVE)
                .build();

        when(walletRepository.findByIdAndOwnerId(wallet.getId(), user.getId())).thenReturn(Optional.of(wallet));

        Wallet changeWalletStatus = walletService.changeStatus(wallet.getId(), user);

        assertEquals(changeWalletStatus.getStatus(), WalletStatus.ACTIVE);
        verify(walletRepository, times(1)).save(changeWalletStatus);
    }

    @Test
    void givenUnExistingWallet_whenChangeStatus_thenExceptionIsThrown() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        UUID walletId = UUID.randomUUID();

        when(walletRepository.findByIdAndOwnerId(walletId, user.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> walletService.changeStatus(walletId, user));
    }

    @Test
    void givenUnExistingWallet_whenChargeUpWallet_thenExceptionIsThrown() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .build();
        BigDecimal amount = new BigDecimal(0);

        when(walletRepository.findByIdAndOwnerId(wallet.getId(), user.getId())).thenReturn(Optional.empty());

        assertThrows(WalletDoNotExistException.class, () -> walletService.chargeUpWallet(user, wallet.getId(), amount));
    }

    @Test
    void givenActiveWallet_whenChargeUpWallet_thenTheAmountIsAddedToTheWalletBalance() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .type(WalletType.DEFAULT)
                .balance(new BigDecimal(10))
                .status(WalletStatus.ACTIVE)
                .build();
        BigDecimal amount = new BigDecimal(15);

        when(walletRepository.findByIdAndOwnerId(wallet.getId(), user.getId())).thenReturn(Optional.of(wallet));

        walletService.chargeUpWallet(user, wallet.getId(), amount);

        assertEquals(wallet.getBalance(), new BigDecimal(25));
        verify(walletRepository, times(1)).save(wallet);
    }

    @Test
    void givenInactiveWallet_whenMakeTransfer_thenTheTransferIsNotExecuted() {

        User sender = User.builder()
                .id(UUID.randomUUID())
                .build();

        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(sender)
                .balance(new BigDecimal(25))
                .status(WalletStatus.INACTIVE)
                .build();

        User receiver = User.builder()
                .id(UUID.randomUUID())
                .username("receiver")
                .build();

        Wallet receiverWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(receiver)
                .balance(new BigDecimal(0))
                .status(WalletStatus.ACTIVE)
                .build();
        List<Wallet> receiverWallets = List.of(receiverWallet);

        TransferRequest transferRequest = TransferRequest.builder()
                .sender(senderWallet.getId())
                .receiver(receiver.getUsername())
                .amount(new BigDecimal(10))
                .build();

        when(walletRepository.findByIdAndOwnerId(transferRequest.getSender(), sender.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findAllByOwnerUsername(receiver.getUsername())).thenReturn(receiverWallets);

        walletService.makeTransfer(sender, transferRequest);

        assertEquals(senderWallet.getBalance(), new BigDecimal(25));
        assertEquals(receiverWallet.getBalance(), new BigDecimal(0));
        verify(walletRepository, never()).save(senderWallet);
        verify(walletRepository, never()).save(receiverWallet);
    }

    @Test
    void givenUnExistingWallet_whenMakeTransfer_thenExceptionIsThrown() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .build();
        TransferRequest transferRequest = TransferRequest.builder()
                .sender(UUID.randomUUID())
                .receiver("receiver")
                .amount(new BigDecimal(10))
                .build();

        when(walletRepository.findByIdAndOwnerId(transferRequest.getSender(), user.getId())).thenReturn(Optional.empty());

        assertThrows(WalletDoNotExistException.class, () -> walletService.makeTransfer(user, transferRequest));
    }

    @Test
    void givenWalletWithNotEnoughAmount_whenMakeTransfer_thenTheTransferIsNotExecuted() {

        User sender = User.builder()
                .id(UUID.randomUUID())
                .build();

        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(sender)
                .balance(new BigDecimal(25))
                .status(WalletStatus.ACTIVE)
                .build();

        User receiver = User.builder()
                .id(UUID.randomUUID())
                .username("receiver")
                .build();

        Wallet receiverWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(receiver)
                .balance(new BigDecimal(0))
                .status(WalletStatus.ACTIVE)
                .build();
        List<Wallet> receiverWallets = List.of(receiverWallet);

        TransferRequest transferRequest = TransferRequest.builder()
                .sender(senderWallet.getId())
                .receiver(receiver.getUsername())
                .amount(new BigDecimal(100))
                .build();

        when(walletRepository.findByIdAndOwnerId(transferRequest.getSender(), sender.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findAllByOwnerUsername(receiver.getUsername())).thenReturn(receiverWallets);

        walletService.makeTransfer(sender, transferRequest);

        assertEquals(senderWallet.getBalance(), new BigDecimal(25));
        assertEquals(receiverWallet.getBalance(), new BigDecimal(0));
        verify(walletRepository, never()).save(senderWallet);
        verify(walletRepository, never()).save(receiverWallet);
    }

    @Test
    void givenHappyPath_whenMakeTransfer() {

        User sender = User.builder()
                .id(UUID.randomUUID())
                .build();

        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(sender)
                .balance(new BigDecimal(25))
                .status(WalletStatus.ACTIVE)
                .build();

        User receiver = User.builder()
                .id(UUID.randomUUID())
                .username("receiver")
                .build();

        Wallet receiverWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(receiver)
                .balance(new BigDecimal(0))
                .status(WalletStatus.ACTIVE)
                .build();
        List<Wallet> receiverWallets = List.of(receiverWallet);

        TransferRequest transferRequest = TransferRequest.builder()
                .sender(senderWallet.getId())
                .receiver(receiver.getUsername())
                .amount(new BigDecimal(10))
                .build();

        when(walletRepository.findByIdAndOwnerId(transferRequest.getSender(), sender.getId())).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findAllByOwnerUsername(receiver.getUsername())).thenReturn(receiverWallets);

        walletService.makeTransfer(sender, transferRequest);

        assertEquals(senderWallet.getBalance(), new BigDecimal(15));
        assertEquals(receiverWallet.getBalance(), new BigDecimal(10));
        verify(walletRepository, times(1)).save(senderWallet);
        verify(walletRepository, times(1)).save(receiverWallet);
    }

    @Test
    void givenWalletWithNotEnoughAmount_whenChargeWallet_thenTheTransferIsNotExecuted() {

        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal(25))
                .status(WalletStatus.ACTIVE)
                .build();

        BigDecimal amount = new BigDecimal(100);

        boolean result = walletService.chargeWallet(senderWallet, amount);

        assertFalse(result);
    }

    @Test
    void givenInactiveWallet_whenChargeWallet_thenTheTransferIsNotExecuted() {

        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal(25))
                .status(WalletStatus.INACTIVE)
                .build();

        BigDecimal amount = new BigDecimal(10);

        boolean result = walletService.chargeWallet(senderWallet, amount);

        assertFalse(result);
    }

    @Test
    void givenHappyPath_whenChargeWallet() {

        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(new BigDecimal(25))
                .status(WalletStatus.ACTIVE)
                .build();

        BigDecimal amount = new BigDecimal(10);

        boolean result = walletService.chargeWallet(senderWallet, amount);

        assertTrue(result);
    }

    @Test
    void givenHappyPath_whenGetWalletsReport() {

        Wallet firstWallet = Wallet.builder()
                .type(WalletType.DEFAULT)
                .balance(new BigDecimal(10))
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet secondWallet = Wallet.builder()
                .type(WalletType.SAVING)
                .balance(new BigDecimal(0))
                .status(WalletStatus.INACTIVE)
                .build();

        Wallet thirdWallet = Wallet.builder()
                .type(WalletType.PAYABLE)
                .balance(new BigDecimal(0))
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet fourthWallet = Wallet.builder()
                .type(WalletType.DEFAULT)
                .balance(new BigDecimal(10))
                .status(WalletStatus.INACTIVE)
                .build();

        Wallet fifthWallet = Wallet.builder()
                .type(WalletType.SAVING)
                .balance(new BigDecimal(10))
                .status(WalletStatus.ACTIVE)
                .build();

        Wallet sixthWallet = Wallet.builder()
                .type(WalletType.PAYABLE)
                .balance(new BigDecimal(10))
                .status(WalletStatus.ACTIVE)
                .build();

        List<Wallet> allWallets = List.of(firstWallet, secondWallet, thirdWallet, fourthWallet, fifthWallet, sixthWallet);

        when(walletRepository.findAll()).thenReturn(allWallets);

        WalletsReport walletsReport = walletService.getWalletsReport();

        assertEquals(walletsReport.getTotalWallets(), 6);
        assertEquals(walletsReport.getActiveWallets(), 4);
        assertEquals(walletsReport.getInactiveWallets(), 2);
        assertEquals(walletsReport.getTotalWalletsAmount(), new BigDecimal(40));

    }
}
