package com.zbs.de.model.dto.price;

import java.util.List;

import com.zbs.de.model.dto.DtoMenuItemRole;

public class DtoPriceMatrixFilters {
	private List<DtoMenuItemRole> roles;
	private List<String> types;
	private List<DtoStationInfo> stations;
	private List<DtoBundleInfo> bundles;

	public List<DtoMenuItemRole> getRoles() {
		return roles;
	}

	public void setRoles(List<DtoMenuItemRole> roles) {
		this.roles = roles;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<DtoStationInfo> getStations() {
		return stations;
	}

	public void setStations(List<DtoStationInfo> stations) {
		this.stations = stations;
	}

	public List<DtoBundleInfo> getBundles() {
		return bundles;
	}

	public void setBundles(List<DtoBundleInfo> bundles) {
		this.bundles = bundles;
	}

}
 