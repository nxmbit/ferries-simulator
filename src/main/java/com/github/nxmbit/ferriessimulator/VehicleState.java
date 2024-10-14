package com.github.nxmbit.ferriessimulator;

public enum VehicleState {
    TRAVELLING_TO_DOCK, // state of traveling to the dock
    AWAITING_ON_DOCK, // state of waiting at the dock
    LOADED_ON_FERRY, // state of being loaded onto the ferry
    UNLOADING_FROM_FERRY, // state of unloading from the ferry (there is a separate queue for exiting!)
    UNLOADED_FROM_FERRY, // state of exiting from the exiting queue
    LOADING_ON_FERRY, // state of loading onto the ferry
    TRAVELLING_FROM_DOCK, // state after leaving the dock after crossing the river
    TURNING_LEFT,
    TURNING_RIGHT,
    GOING_STRAIGHT_UP,
    GOING_STRAIGHT_DOWN,
    APPROACHING_TURN
}
