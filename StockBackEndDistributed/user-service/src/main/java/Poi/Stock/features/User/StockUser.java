package Poi.Stock.features.User;

import java.util.ArrayList;
import java.util.List;

import Poi.Stock.features.WatchList.WatchList;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "StockUser")
public class StockUser {
	@Id
	String id;
	String username;
	String password;
	Integer Asset;
	Integer availableAsset; // 추가
	// 가지고있는 주식
	// 사용자가 보유한 주식들
	@OneToMany(mappedBy = "stockUser", cascade = CascadeType.ALL)
	private List<HaveStock> holdings = new ArrayList<>();
	@OneToMany(mappedBy = "stockUser", cascade = CascadeType.ALL)
	private List<WatchList> watchLists = new ArrayList<>();
}
