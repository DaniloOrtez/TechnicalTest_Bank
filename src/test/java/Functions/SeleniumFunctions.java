package Functions;

import StepDefinitions.Hooks;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

public class SeleniumFunctions {

    WebDriver driver;

    public SeleniumFunctions(){
        driver = Hooks.driver;
    }

    // Json Page file path
    public static String PageFilePath = "src/test/resources/Pages/";
    public static String FileName = "";

    // Scenario Test Data
    public static String ElementText = "";
    public static List<String> apiPokemonDataResponse;

    // test properties config
    private static final Properties prop = new Properties();
    private static final InputStream in = CreateDriver.class.getResourceAsStream( "../test.properties");

    // Entities of DOM pages - Json
    public static String GetFieldBy = "";
    public static String ValueToFind = "";

    // Explicits wait
    public static int EXPLICIT_TIMEOUT = 15;

    //******************************************************************************************************************
    //******************************************* WEB FUNCTIONS ********************************************************
    //******************************************************************************************************************

    // Read Json file
    public static Object readJson() throws FileNotFoundException {
        FileReader reader = new FileReader(PageFilePath + FileName);
        try{
            JSONParser jsonParser = new JSONParser();
            return jsonParser.parse(reader);
        }catch (NullPointerException| IOException | ParseException e){
            System.out.println("ReadEntity: No existe el archivo " + FileName);
            throw new IllegalStateException("ReadEntity: No existe el archivo " + FileName, e);
        }
    }
    //******************************************************************************************************************

    // Read the property file
    public String readProperties(String property) throws IOException {
        prop.load(in);
        return prop.getProperty(property);
    }
    //******************************************************************************************************************

    // Read entity into the Jason file
    public static @NotNull JSONObject ReadEntity(String element) throws Exception {
        JSONObject Entity;
        JSONObject jsonObject = (JSONObject) readJson();
        assert jsonObject != null;
        Entity = (JSONObject) jsonObject.get(element);
        return Entity;
    }
    //******************************************************************************************************************

    // Get element of the entity in the Jason file
    public static By getCompleteElement(String element) throws Exception {
        By result = null;
        JSONObject Entity = ReadEntity(element);

        GetFieldBy = (String) Entity.get("GetFieldBy");
        ValueToFind = (String) Entity.get("ValueToFind");

        if ("className".equalsIgnoreCase(GetFieldBy)) {
            result = By.className(ValueToFind);
        } else if ("cssSelector".equalsIgnoreCase(GetFieldBy)) {
            result = By.cssSelector(ValueToFind);
        } else if ("id".equalsIgnoreCase(GetFieldBy)) {
            result = By.id(ValueToFind);
        } else if ("linkText".equalsIgnoreCase(GetFieldBy)) {
            result = By.linkText(ValueToFind);
        } else if ("name".equalsIgnoreCase(GetFieldBy)) {
            result = By.name(ValueToFind);
        } else if ("link".equalsIgnoreCase(GetFieldBy)) {
            result = By.partialLinkText(ValueToFind);
        } else if ("tagName".equalsIgnoreCase(GetFieldBy)) {
            result = By.tagName(ValueToFind);
        } else if ("xpath".equalsIgnoreCase(GetFieldBy)) {
            result = By.xpath(ValueToFind);
        }
        return result;

    }
    //******************************************************************************************************************

    // Click Function in a specific element in the DOM
    public void ClickOnElment(String element, String name) throws Exception {
        String baseElement = SeleniumFunctions.getEntityValue(element);
        String SeleniumElement = baseElement + name + "']";
        driver.findElement(By.xpath(SeleniumElement)).click();
    }
    //******************************************************************************************************************

    // Read the entity 'ValueToFind' including in the Jason File
    public static String getEntityValue(String element) throws Exception {
        JSONObject Entity = ReadEntity(element);
        ValueToFind = (String) Entity.get("ValueToFind");
        return ValueToFind;
    }
    //******************************************************************************************************************

    // Catch y return the text from a web object in the DOM given an editable xpath
    public String getObjectText(String xpath){
        ElementText = driver.findElement(By.xpath(xpath)).getText();
        return ElementText;
    }
    //******************************************************************************************************************

    // Catch y return the text from a web object in the DOM
    public String GetTextElement(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_TIMEOUT, 1));
        wait.until(ExpectedConditions.presenceOfElementLocated(SeleniumElement));
        ElementText = driver.findElement(SeleniumElement).getText();
        return ElementText;
    }
    //******************************************************************************************************************

    // Click Function in a specific element in the DOM
    public void ClickJSElement(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript("arguments[0].click()", driver.findElement(SeleniumElement));
    }
    //******************************************************************************************************************

    // Read the DOM information and return the API data to compare results
    public void getPokemonData(String valueToFind, String element, String URL) throws Exception {
        String foundValue;
        String valor;
        String valorXpath;
        String valueToAdd;
        int rowCount;
        String CompleteXpathElement;
        List<String> objectWebList = new ArrayList<>();

        String baseXpath = SeleniumFunctions.getEntityValue(element);
        String apiBaseURL = SeleniumFunctions.getEntityValue(URL);

        switch (element) {
            case "stats" -> {
                rowCount = driver.findElements(By.xpath(baseXpath + "//tr")).size();
                for (int i = 2; i < rowCount; i++) {
                    CompleteXpathElement = baseXpath + "//tr[" + i + "]//th";
                    foundValue = getObjectText(CompleteXpathElement);
                    if (foundValue.equals("Sp. Atk:")) {
                        foundValue = "special attack:";
                    } else if (foundValue.equals("Sp. Def:")) {
                        foundValue = "special defense:";
                    }

                    valorXpath = baseXpath + "//tr[" + i + "]//td[" + 1 + "]";
                    valor = getObjectText(valorXpath);
                    valueToAdd = foundValue + " " + valor;
                    objectWebList.add(valueToAdd.toLowerCase());

                }
                apiPokemonDataResponse = apiFunctions.readAllDetails(valueToFind, element, apiBaseURL);
                Assert.assertEquals("The data is no equal ", objectWebList, apiPokemonDataResponse);
            }
            case "abilities" -> {
                rowCount = driver.findElements(By.xpath(baseXpath)).size();
                for (int i = 1; i <= rowCount; i++) {
                    CompleteXpathElement = baseXpath + "[" + i + "]";
                    foundValue = getObjectText(CompleteXpathElement).toLowerCase();
                    objectWebList.add(foundValue);
                }
                apiPokemonDataResponse = apiFunctions.readAllDetails(valueToFind, element, apiBaseURL);
                Assert.assertEquals("The data is no equal ", objectWebList, apiPokemonDataResponse);
            }
            case "types" -> {
                apiPokemonDataResponse = apiFunctions.readAllDetails(valueToFind, element, apiBaseURL);

                for (String type : apiPokemonDataResponse){
                    CompleteXpathElement = baseXpath + type + "']";
                    foundValue = getObjectText(CompleteXpathElement).toLowerCase();
                    objectWebList.add(foundValue);
                }
                Assert.assertEquals("The data is no equal ", apiPokemonDataResponse, objectWebList);
            }
            case "effect_entries" -> {
                String effectToFind;
                String ppText = "PP";
                String ppAPI;
                String ppXpath;
                String ppValue;
                foundValue = GetTextElement(element);

                effectToFind = valueToFind.toLowerCase().replace(" ", "-");
                apiPokemonDataResponse = apiFunctions.readAllDetails(effectToFind, element, apiBaseURL);

                //String[] entireEffect = foundValue.split(" T");
                //String effectWEB = entireEffect[0];
                objectWebList.add(foundValue);

                if (apiPokemonDataResponse != null) {
                    ppAPI = apiPokemonDataResponse.get(1);
                    foundValue = SeleniumFunctions.getEntityValue(ppText);
                    ppXpath = foundValue + ppAPI + "')]";
                    ppValue = getObjectText(ppXpath);
                    String[] splitPP = ppValue.split("\n");
                    String usePP = splitPP[0];
                    objectWebList.add(usePP);
                }
                Assert.assertEquals("The data is no equal ", objectWebList, apiPokemonDataResponse);
            }
            case "ability" -> {
                String[] splitName;
                apiPokemonDataResponse = apiFunctions.readAllDetails(valueToFind, element, apiBaseURL);
                for (String name : apiPokemonDataResponse) {
                    String pokemonName = name.toUpperCase().charAt(0) + name.substring(1).toLowerCase();
                    splitName = pokemonName.split("-");
                    String firstSplit = splitName[0];
                    splitName = firstSplit.split(" ");
                    String useName = splitName[0];
                    CompleteXpathElement = baseXpath + "'" + useName + "')]";
                    foundValue = getObjectText(CompleteXpathElement).toLowerCase();
                    objectWebList.add(foundValue.replace("-", " "));
                }
                Assert.assertEquals("The data is no equal ", apiPokemonDataResponse, objectWebList);
            }

            default -> System.out.println("The options doesn't exist");
        }

        System.out.println("The pokemon WEB stats are: " + objectWebList);
        System.out.println("The pokemon API stats are: " + apiPokemonDataResponse);

    }
    //******************************************************************************************************************
    //******************************************************************************************************************

    // Scroll Java Script to element
    public void scrollToElement(String element) throws Exception {
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        JavascriptExecutor jse = (JavascriptExecutor)driver;
        jse.executeScript("arguments[0].scrollIntoView();", driver.findElement(SeleniumElement));
    }
    //******************************************************************************************************************

    // Select option from DropDown
    public Select selectOption(String element) throws Exception{
        By SeleniumElement = SeleniumFunctions.getCompleteElement(element);
        return new Select(driver.findElement(SeleniumElement));
    }
    //******************************************************************************************************************


    // Function to retrieve the WEB Calculate result and compare it with the result from the function  'mortgageCalculate'
    public void TakeDataElement(String element_1, String element_2) throws Exception {

        String homePrice = getEntityValue("Home Price Value");
        float hPrice = Float.parseFloat(homePrice);

        String dPayment = getEntityValue("Down Payment Value");
        float downPayment = Float.parseFloat(dPayment)/100;
        float Mortgage = hPrice - (hPrice * downPayment);

        String ratePercent = getEntityValue("Interest Rate Value");
        float rPercent = (Float.parseFloat(ratePercent)/100)/12;

        String year = getEntityValue("Loan Term Value").replace(" Years", "");
        int Months = Integer.parseInt(year)*12;

        Double resultFunction = mortgageCalculate(Mortgage, rPercent, Months);

        ClickJSElement(element_1);
        String resultWEB = GetTextElement(element_2);

        String webCalculated = (resultWEB.replace("$", ""));
        String functionCalculated = (String.valueOf(Math.round(resultFunction * 100d) / 100d));

        Assert.assertEquals("The results aren't equals", functionCalculated, webCalculated);

        System.out.println("The WEB result is: " + webCalculated);
        System.out.println("The Function result is: " + functionCalculated);

    }
    //******************************************************************************************************************


    // Functions to calculate the mortgage with the Json file data sending from the TakenDataElement function
    public static Double mortgageCalculate(float cMortgage,float rPercent, int years){

        double resultFunction;
        resultFunction = cMortgage * (rPercent * (Math.pow((1 + rPercent), years)) / (Math.pow((1 + rPercent), years)-1));
        return resultFunction;
    }


//******************************************************************************************************************

    /** SCREENSHOT WITH SELENIUM **/
    public void ScreenShot(String TestCaptura) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        String screenShotName = readProperties("ScreenShotPath") + "\\" + readProperties("browser") + "\\" + TestCaptura + "_(" + dateFormat.format(GregorianCalendar.getInstance().getTime()) + ")";
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(String.format("%s.png", screenShotName)));
    }

}
