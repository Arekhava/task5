package by.epam.main;

import by.epam.main.entity.Ship;
import by.epam.main.exception.PortException;
import by.epam.main.parser.ShipActionParser;
import by.epam.main.reader.ShipFileReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class main {

        public static void main(String[] args) throws PortException {
            ShipFileReader reader = new ShipFileReader();
            ArrayList<String> shipStrings = reader.readFile("data/data.txt");
            ShipActionParser parser = new ShipActionParser();
            List<Ship> ships = parser.parseShipAction(shipStrings);

            ExecutorService executorService = Executors.newFixedThreadPool(ships.size());
            ships.forEach(executorService::execute);
            executorService.shutdown();

        }
    }



