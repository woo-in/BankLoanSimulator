package bankapp.loan.servicing.service.lifecycle;

import bankapp.loan.exceptions.InvalidRepaymentScheduleException;
import bankapp.loan.exceptions.InvalidRepaymentStatus;
import bankapp.loan.servicing.model.RepaymentSchedule;
import bankapp.loan.servicing.model.RepaymentStatus;
import bankapp.loan.servicing.service.core.RepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DefaultRepaymentStatusService implements RepaymentStatusService {

    private final RepaymentScheduleService repaymentScheduleService;

    @Autowired
    public DefaultRepaymentStatusService(RepaymentScheduleService repaymentScheduleService){
        this.repaymentScheduleService = repaymentScheduleService;
    }

//    PLANNED, // 계획 , 먼 미래
//    PENDING, // 대기
//    COMPLETE, // 완료
//    MERGED // 병합됨

    @Override
    @Transactional
    public void changeRepaymentStatus(RepaymentSchedule schedule, RepaymentStatus targetStatus){

        if(schedule == null){
            throw new InvalidRepaymentScheduleException("스케줄이 올바르지 않아 , 상태를 바꿀 수 없습니다.");
        }

        if(targetStatus == RepaymentStatus.COMPLETE){
            changeRepaymentStatusToComplete(schedule);
        }
        else if(targetStatus == RepaymentStatus.PENDING){
            changeRepaymentStatusToPending(schedule);
        }
        else if(targetStatus == RepaymentStatus.MERGED){
            changeRepaymentStatusToMerge(schedule);
        }
        else{
            throw new InvalidRepaymentScheduleException("스케줄이 올바르지 않아 , 상태를 바꿀 수 없습니다.");
        }

    }

    private void changeRepaymentStatusToComplete(RepaymentSchedule schedule){
        // todo : 적절한 필터 추가 가능
        if(schedule.getStatus() == RepaymentStatus.PLANNED || schedule.getTotalAmount().compareTo(BigDecimal.ZERO) > 0){
            throw new InvalidRepaymentStatus("can not convert to COMPLETE status.");
        }
        repaymentScheduleService.updateRepaymentStatus(schedule , RepaymentStatus.COMPLETE);

    }

    private void changeRepaymentStatusToPending(RepaymentSchedule schedule){
        // todo : 적절한 필터 추가 가능
        if(schedule.getStatus() != RepaymentStatus.PLANNED){
            throw new InvalidRepaymentStatus("can not convert to PLANNED status.");
        }
        repaymentScheduleService.updateRepaymentStatus(schedule , RepaymentStatus.PENDING);

    }

    private void changeRepaymentStatusToMerge(RepaymentSchedule schedule){
        // todo : 적절한 필터 추가 가능
        repaymentScheduleService.updateRepaymentStatus(schedule , RepaymentStatus.MERGED);
    }







}
