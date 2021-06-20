package by.epam.main.entity;

import by.epam.main.util.ShipIdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Random;

public class Ship implements Runnable{
    private static final Logger logger = LogManager.getLogger(Ship.class);

    private static final int SHIP_CAPACITY=20;
    private final long shipId;
    private ShipTask shipTask;
    private State state;
    private int containerAmount;


    public Ship(int containerAmount, ShipTask shipTask) {
        this.shipId = ShipIdGenerator.generateShapeId();
        this.shipTask = shipTask;
        state = State.ENTER;
        this.containerAmount = containerAmount;
        logger.info("New Ship with id {} and {} containers was created", shipId, containerAmount);
    }

    public static int getShipCapacity() {
        return SHIP_CAPACITY;
    }

    public long getShipId() {
        return shipId;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public int getContainerAmount() {
        return containerAmount;
    }

    public ShipTask getShipTask() {
        return shipTask;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Ship{");
        sb.append("shipId=").append(shipId);
        sb.append(", shipTask=").append(shipTask);
        sb.append(", state=").append(state);
        sb.append(", containerAmount=").append(containerAmount);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return shipId == ship.shipId && containerAmount == ship.containerAmount && shipTask == ship.shipTask && state == ship.state;
    }

    @Override
    public int hashCode() {
        int result = SHIP_CAPACITY;
        result = result * 42 + containerAmount;
        return result;
    }

    @Override
    public void run() {
        logger.info("Ship {} started {}", shipId, shipTask);
        Terminal terminal = Terminal.getInstance();
        Pier pier = terminal.arrivePier();
        pier.processShip(this);
        terminal.leavePier(pier);
        logger.info("Ship {} finished {}", shipId, shipTask);

    }
}
