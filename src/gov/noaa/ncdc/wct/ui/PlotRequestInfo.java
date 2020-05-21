package gov.noaa.ncdc.wct.ui;

import java.awt.Color;

public class PlotRequestInfo {

	enum PlotType { LINE, BAR, AREA };
	private String[] variables;
	private String[] units;
	private PlotType[] plotTypes;
	private Color[] plotColors;
	public void setVariables(String[] variables) {
		this.variables = variables;
	}
	public String[] getVariables() {
		return variables;
	}
	public void setPlotTypes(PlotType[] plotTypes) {
		this.plotTypes = plotTypes;
	}
	public PlotType[] getPlotTypes() {
		return plotTypes;
	}
	public void setPlotColors(Color[] plotColors) {
		this.plotColors = plotColors;
	}
	public Color[] getPlotColors() {
		return plotColors;
	}
	public void setUnits(String[] units) {
		this.units = units;
	}
	public String[] getUnits() {
		return units;
	}
}