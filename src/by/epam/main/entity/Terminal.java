package by.epam.main.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Terminal {
    private static final Logger logger = LogManager.getLogger(Terminal.class);

    private static final String PROPERTIES_PATH = "port.properties";
    private static final String PROPERTIES_PIER_AMOUNT = "pier_amount";
    private static final String PROPERTIES_CAPACITY = "capacity";
    private static final String PROPERTIES_SHIP_CAPACITY = "ship_capacity";
    private static final String PROPERTIES_CONTAINER_AMOUNT = "container_amount";

    private static final double MAX_LOAD_FACTOR = 0.75;
    private static final double MIN_LOAD_FACTOR = 0.25;
    private static final long TIMER_TASK_DELAY = 500L;
    private static final long TIMER_TASK_PERIOD = 200L;

    private static final AtomicBoolean instanceInitialized = new AtomicBoolean(false);

    private final int PIER_AMOUNT;
    private final int CAPACITY;
    private final int SHIP_CAPACITY;
    private static Terminal instance;
    private final ArrayDeque<Pier> availablePiers = new ArrayDeque<>();
    private final ArrayDeque<Pier> busyPiers = new ArrayDeque<>();
    private Lock pierLock = new ReentrantLock();
    private Condition freePierCondition = pierLock.newCondition();
    private Lock storageLock = new ReentrantLock();
    private Condition unavaliableStorageLock = storageLock.newCondition();
    private Condition avaliableStorageLock = storageLock.newCondition();
    private int totalContainersAmount;
    public Terminal() {
        URL resource = getClass().getClassLoader().getResource(PROPERTIES_PATH);
        Properties properties = new Properties();
        if (resource != null) {
            File file = new File(resource.getFile());
            try (FileReader fileReader = new FileReader(file)) {
                properties.load(fileReader);
            } catch (IOException e) {
                logger.error("Properties file is unavailable");
            }
        }
        String stringPierAmount = properties.getProperty(PROPERTIES_PIER_AMOUNT);
        PIER_AMOUNT = stringPierAmount == null ? 2 : Integer.parseInt(stringPierAmount);
        for (int i = 0; i < PIER_AMOUNT; i++) {
            availablePiers.addLast(new Pier());
        }
        String stringCapacity = properties.getProperty(PROPERTIES_CAPACITY);
        CAPACITY = stringCapacity == null ? 17 : Integer.parseInt(stringCapacity);
        String stringShipCapacity = properties.getProperty(PROPERTIES_SHIP_CAPACITY);
        SHIP_CAPACITY = stringShipCapacity == null ? 5 : Integer.parseInt(stringShipCapacity);
        String stringContainerAmount = properties.getProperty(PROPERTIES_CONTAINER_AMOUNT);
        totalContainersAmount = stringContainerAmount == null ? 12 : Integer.parseInt(stringContainerAmount);
        setStorageTask();
    }

    public static Terminal getInstance() {
        while (instance == null) {
            if (instanceInitialized.compareAndSet(false, true)) {
                instance = new Terminal();
            }
        }
        return instance;
    }

    public Pier arrivePier() {
        logger.info("Starting arriving to Pier");
        try {
            pierLock.lock();
            try {
                if (availablePiers.isEmpty()) {
                    logger.info("All piers are busy, waiting...");
                    freePierCondition.await();
                }
            } catch (InterruptedException e) {
                logger.error("Error while arriving pier: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
            Pier pier = availablePiers.removeFirst();
            busyPiers.addLast(pier);
            logger.info("Arrived to pier: {}", pier);
            return pier;
        } finally {
            pierLock.unlock();
        }
    }

    public void leavePier(Pier pier) {
        logger.info("Starting leaving to Pier");
        try {
            pierLock.lock();
            busyPiers.remove(pier);
            availablePiers.addLast(pier);
            freePierCondition.signal();
            logger.info("Left pier: {}", pier);
        } finally {
            pierLock.unlock();
        }
    }

    public void loadContainers(int containersAmount) {
        try {
            storageLock.lock();
            logger.info("Starting load {} containers", SHIP_CAPACITY - containersAmount);
            if (totalContainersAmount < SHIP_CAPACITY - containersAmount) {
                logger.error("There are not enough containers ({}) for ship", containersAmount);
                Thread.currentThread().interrupt();
            }
            totalContainersAmount -= (SHIP_CAPACITY - containersAmount);
            logger.info("Finishing load {} containers, Terminal has {} containers", SHIP_CAPACITY - containersAmount,
                    totalContainersAmount);
        } finally {
            storageLock.unlock();
        }
    }

    public void unloadContainers(int containersAmount) {
        try {
            storageLock.lock();
            logger.info("Starting unload {} containers", containersAmount);
            if (totalContainersAmount + containersAmount > CAPACITY) {
                logger.error("There are too much containers ({}) for terminal {}", containersAmount, totalContainersAmount);
                Thread.currentThread().interrupt();
            }
            totalContainersAmount += containersAmount;
            logger.info("Finishing unload {} containers, Terminal has {} containers", containersAmount, totalContainersAmount);
        } finally {
            storageLock.unlock();
        }
    }

    private void setStorageTask() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    storageLock.lock();
                    double loadFactor = (double) totalContainersAmount / CAPACITY;
                    if (loadFactor > MAX_LOAD_FACTOR) {
                        totalContainersAmount *= 0.8;
                        logger.debug("Too many containers, new amount is: {}", totalContainersAmount);
                    } else if (loadFactor < MIN_LOAD_FACTOR) {
                        totalContainersAmount *= 1.2;
                        logger.debug("Too few containers, new amount is: {}", totalContainersAmount);
                    }
                } finally {
                    storageLock.unlock();
                }
            }
        }, TIMER_TASK_DELAY, TIMER_TASK_PERIOD);
    }
}