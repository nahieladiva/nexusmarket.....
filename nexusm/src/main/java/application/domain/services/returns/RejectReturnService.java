package application.domain.services.returns;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.ReturnRequest;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ReturnId;

import java.util.Objects;

/**
 * An administrator rejects a requested return.
 */
@DomainService
public class RejectReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ClockPort clock;

    public RejectReturnService(ReturnRequestRepository returnRequestRepository, ClockPort clock) {
        this.returnRequestRepository = Objects.requireNonNull(returnRequestRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public ReturnRequest execute(User administrator, ReturnId returnId) {
        administrator.requireRole(UserRole.ADMINISTRATOR);
        ReturnRequest returnRequest = returnRequestRepository.findById(returnId)
            .orElseThrow(() -> new ResourceNotFoundException("Return", returnId.toString()));
        returnRequest.reject(clock.now());
        return returnRequestRepository.save(returnRequest);
    }
}
