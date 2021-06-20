package by.epam.main.entity;

import by.epam.main.util.PierIdGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Pier {
    private static final Logger logger = LogManager.getLogger(Pier.class);
    private final long pierId;
    private static final int MIN_MILLISECONDS = 1;
    private static final int MAX_MILLISECONDS = 1000;

    public Pier() {
        this.pierId = PierIdGenerator.generatePierId();
        logger.info("new Pier with id {} was created", pierId);
    }

    public long getPierId() {
        return pierId;
    }

    public void processShip (Ship ship){
        logger.info("Pier {} started {} of Ship {} with {} containers", pierId, ship.getShipTask(), ship.getShipId(),
                ship.getContainerAmount());
        ship.setState(State.PROCESS);
        Random random = new Random();
        int timeout = random.nextInt(MAX_MILLISECONDS - MIN_MILLISECONDS + 1);
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            logger.error("Exception in Pier: {}", e.getMessage());
            Thread.currentThread().isInterrupted();
        }

        ShipTask currentTask = ship.getShipTask();
        Terminal terminal = Terminal.getInstance();
        switch (currentTask) {
            case LOAD -> terminal.loadContainers(ship.getContainerAmount());
            case UNLOAD -> terminal.unloadContainers(ship.getContainerAmount());
        }
        ship.setState(State.FINISH);
        logger.info("Pier {} finished {} of Ship {}", pierId, currentTask, ship.getShipId());
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Pier{");
        sb.append("pierId=").append(pierId);
        sb.append('}');
        return sb.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pier pier = (Pier) o;
        return pierId == pier.pierId;
    }
}
