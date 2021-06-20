package by.epam.main.parser;

import by.epam.main.entity.Ship;
import by.epam.main.entity.ShipTask;
import by.epam.main.exception.PortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

    public class ShipActionParser {
        private final static Logger logger = LogManager.getLogger(ShipActionParser.class);
        public static final String DATA_SPLIT = " ";

        public List<Ship> parseShipAction(ArrayList<String> stringActions) throws PortException {

            		if (stringActions == null) {
            			throw new PortException("data is null");
            		}
            		List<Ship> ships = new ArrayList<>();
            		for (String lineData : stringActions) {
            			String[] shipsAmountAndState = lineData.split(DATA_SPLIT);
            			int containerAmount = Integer.parseInt(shipsAmountAndState[0]);
            			ShipTask task = ShipTask.valueOf(shipsAmountAndState[1]);
            			Ship ship = new Ship(containerAmount, task);
            			ships.add(ship);
            		}
            		logger.info("Ships from parseMethod: {}", ships);
            		return ships;
            	}


        }
