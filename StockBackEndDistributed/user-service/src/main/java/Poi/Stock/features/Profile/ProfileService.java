package Poi.Stock.features.Profile;
 
import Poi.Stock.DTO.user.ProfileDTO;
import Poi.Stock.features.User.StockUser;
import Poi.Stock.repository.StockUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
 
@Service
@RequiredArgsConstructor
public class ProfileService {
 
    private final StockUserRepository stockUserRepository;
 
    public ProfileDTO getProfile(String userId) {
        StockUser user = stockUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));
        return new ProfileDTO(user.getId());
    }
}
 