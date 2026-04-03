package se.jensen.johanna.auctionsite.model.enums;

public enum AuctionStatus {
  INACTIVE, PLANNED, ACTIVE, SOLD, EXPIRED, ACCEPTED_NOT_MET, COMPLETED;

  public boolean isAvailableToLaunch() {
    return this == INACTIVE || this == EXPIRED || this == ACCEPTED_NOT_MET;

  }
}
