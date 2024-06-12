package com.github.nxmbit.ferriessimulator;

public enum VehicleState {
    TRAVELLING_TO_DOCK, //stan podrozy do przystani
    AWAITING_ON_DOCK, //stan oczekiwania na przystani
    LOADED_ON_FERRY, //stan zaladowania na promie
    UNLOADING_FROM_FERRY, //stan rozladunku z promu (tu jest osobna kolejka do zjazdu!)
    TRAVELLING_FROM_DOCK, //stan po wyjezdzie z przystani po przekroczeniu rzeki
    TURNING_LEFT,
    TURNING_RIGHT,
    GOING_STRAIGHT_UP,
    GOING_STRAIGHT_DOWN,
    APPROACHING_TURN
}
