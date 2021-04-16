package com.seamCarving.core;

public class SeamCarvingDto {

    private String inputImagePath;
    private Integer outputNumColumns;
    private Integer outputNumRows;
    private EnergyType energyType;
    private String outputImagePath;
    private boolean straightSeam;
    private boolean addInterpolation;

    public SeamCarvingDto() {
    }

    public SeamCarvingDto(String inputImagePath, Integer outputNumColumns, Integer outputNumRows, EnergyType energyType, String outputImagePath, boolean straightSeam, boolean addInterpolation) {
        this.inputImagePath = inputImagePath;
        this.outputNumColumns = outputNumColumns;
        this.outputNumRows = outputNumRows;
        this.energyType = energyType;
        this.outputImagePath = outputImagePath;
        this.straightSeam = straightSeam;
        this.addInterpolation = addInterpolation;
    }

    public String getInputImagePath() {
        return inputImagePath;
    }

    public void setInputImagePath(String inputImagePath) {
        this.inputImagePath = inputImagePath;
    }

    public Integer getOutputNumColumns() {
        return outputNumColumns;
    }

    public void setOutputNumColumns(Integer outputNumColumns) {
        this.outputNumColumns = outputNumColumns;
    }

    public Integer getOutputNumRows() {
        return outputNumRows;
    }

    public void setOutputNumRows(Integer outputNumRows) {
        this.outputNumRows = outputNumRows;
    }

    public String getOutputImagePath() {
        return outputImagePath;
    }

    public void setOutputImagePath(String outputImagePath) {
        this.outputImagePath = outputImagePath;
    }

    public EnergyType getEnergyType() {
        return energyType;
    }

    public void setEnergyType(EnergyType energyType) {
        this.energyType = energyType;
    }

    public boolean isStraightSeam() {
        return straightSeam;
    }

    public void setStraightSeam(boolean straightSeam) {
        this.straightSeam = straightSeam;
    }

    public boolean isAddInterpolation() {
        return addInterpolation;
    }

    public void setAddInterpolation(boolean addInterpolation) {
        this.addInterpolation = addInterpolation;
    }
}
