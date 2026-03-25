package Poi.Stock.features.FailedOrder;

import java.time.LocalDateTime;

import Poi.Stock.util.EnumUtil.FailStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedOrder {
	@Id
	@GeneratedValue
	private Long id;
	private String userId;
	private String stockCode;
	private String reason;
	private int retryCount;
	private LocalDateTime failedAt;

	@Enumerated(EnumType.STRING)
	private FailStatus status; // PENDING_REVIEW, PERMANENT_FAIL
}