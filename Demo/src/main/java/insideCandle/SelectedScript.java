package insideCandle;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

public class SelectedScript {
	FileInputStream fis;
	FileOutputStream fout;
	XSSFWorkbook workbook;
	XSSFSheet sheetSymbolList, sheetTokenList;
	int trow;
	String kf_session, x_csrftoken;
	Response reponseOfInstrumentToken;
	Date date = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	int rowSymbol, rowToken;
	static ArrayList<String> stockName;
	ChromeOptions option;
	Row rowScriptList, rowTokenList;
	Date todayDate;

	public static void main(String args[]) throws IOException, InvalidFormatException, InterruptedException {
		SelectedScript script = new SelectedScript();
		stockName = new ArrayList<String>();
		script.getToken();
	}

	public void getToken() throws InterruptedException, IOException {

		fis = new FileInputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
		workbook = new XSSFWorkbook(fis);
		sheetSymbolList = workbook.getSheet("symbol_indicators");
		sheetTokenList = workbook.getSheet("token");
		rowSymbol = sheetSymbolList.getLastRowNum();
		rowToken = sheetTokenList.getLastRowNum();

		WebDriver wd = new ChromeDriver();
		WebDriverWait wc = new WebDriverWait(wd, 60);

		wd.get("https://www.nseindia.com/live_market/dynaContent/live_watch/pre_open_market/pre_open_market.htm");
		Thread.sleep(10000);

		List<WebElement> myElements = wd.findElements(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr"));

		for (int i = 3; i <= myElements.size(); i++) {
			if (stockName.size() >= 5) {
				break;
			}

			if (Double
					.parseDouble(wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[4]"))
							.getText().replace(",", "")) < 2000.00
					&& Double.parseDouble(
							wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[4]"))
									.getText().replace(",", "")) > 125.00) {
				stockName.add(
						wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[1]")).getText());
				System.out.println(
						wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[1]")).getText());
			}
		}

		for (int i = myElements.size() - 1; i > 0; i--) {
			if (stockName.size() >= 10) {
				break;
			}

			if (Double
					.parseDouble(wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[4]"))
							.getText().replace(",", "")) < 2000.00
					&& Double.parseDouble(
							wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[4]"))
									.getText().replace(",", "")) > 125.00) {
				stockName.add(
						wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[1]")).getText());
				System.out.println(
						wd.findElement(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr[" + i + "]/td[1]")).getText());

			}
		}

		wd.get("https://kite.zerodha.com/");
		wc.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='text']")));
		Thread.sleep(500);
		wd.findElement(By.xpath("//input[@type='text']")).click();
		wd.findElement(By.xpath("//input[@type='text']")).sendKeys("YG7487");
		wc.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@type='password']")));
		Thread.sleep(500);
		wd.findElement(By.xpath("//input[@type='password']")).click();
		wd.findElement(By.xpath("//input[@type='password']")).sendKeys("suju1988");
		wd.findElement(By.xpath("//button[@type='submit']")).click();
		wc.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[@type='submit']")));
		wd.findElement(By.xpath("//input[@type='password']")).click();
		wd.findElement(By.xpath("//input[@type='password']")).sendKeys("261988");
		wd.findElement(By.xpath("//button[@type='submit']")).click();
		wc.until(ExpectedConditions.elementToBeClickable(By.xpath("//img[@alt='Kite logo']")));
		Thread.sleep(2000);

		WebStorage webStorage = (WebStorage) wd;
		LocalStorage localStorage = webStorage.getLocalStorage();

		kf_session = wd.manage().getCookieNamed("kf_session").getValue();
		x_csrftoken = localStorage.getItem("__storejs_kite_public_token").replace("\"", "");
		rowTokenList = sheetTokenList.createRow(++rowToken);
		rowTokenList.createCell(0).setCellValue(kf_session);
		rowTokenList.createCell(1).setCellValue(x_csrftoken);

		for (int i = 0; i <= stockName.size(); i++) {
			System.out.append(stockName.get(i));
			reponseOfInstrumentToken = given()
					.headers("content-type", "application/x-www-form-urlencoded", "accept", "application/json",
							"cookie",
							"__cfduid=d21217c0877ef1707cd1b611d605c488c1555093243;kf_session=" + kf_session + "",
							"x-csrftoken", x_csrftoken)
					.formParam("segment", "NSE").formParam("tradingsymbol", stockName.get(i))
					.formParam("watch_id", "2021834").formParam("weight", "1")
					.post("https://kite.zerodha.com/api/marketwatch/2021834/items").then().extract().response();
			System.out.println(reponseOfInstrumentToken.asString());
			JsonObject obj = (JsonObject) new JsonParser().parse(reponseOfInstrumentToken.asString());
			JsonObject data = (JsonObject) obj.get("data");

			rowScriptList = sheetSymbolList.createRow(++rowSymbol);

			rowScriptList.createCell(0).setCellValue(formatter.format(date));
			rowScriptList.createCell(1).setCellValue(stockName.get(i));
			rowScriptList.createCell(2).setCellValue(data.get("instrument_token").getAsString());

			fis.close();
			FileOutputStream fos = new FileOutputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
			workbook.write(fos);
			fos.close();
		}

	}

}
