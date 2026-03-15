package Poi.Stock.features.User;

import java.util.ArrayList;
import java.util.List;

import Poi.Stock.features.WatchList.WatchList;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "StockUser")
public class StockUser {
	@Id
	String id;
	String username;
	String password;
	Integer Asset;
	// 가지고있는 주식
	// 사용자가 보유한 주식들
	@OneToMany(mappedBy = "stockUser", cascade = CascadeType.ALL)
	private List<HaveStock> holdings = new ArrayList<>();
	@OneToMany(mappedBy = "stockUser", cascade = CascadeType.ALL)
	private List<WatchList> watchLists = new ArrayList<>();

	public Integer getAsset() {
		return Asset;
	}

	public void setAsset(Integer asset) {
		Asset = asset;
	}

	public List<HaveStock> getHoldings() {
		return holdings;
	}

	public void setHoldings(List<HaveStock> holdings) {
		this.holdings = holdings;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
