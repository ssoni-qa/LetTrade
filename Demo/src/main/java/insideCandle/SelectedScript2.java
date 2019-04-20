package insideCandle;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.restassured.response.Response;

public class SelectedScript2 {
	static FileInputStream fis;
	FileOutputStream fout;
	static XSSFWorkbook workbook;
	static XSSFSheet sheetSymbolList, sheetMagicCandleList;
	static int trow;
	String kf_session;
	Response reponseOfInstrumentToken;
	Date date = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	int rowSymbol, rowMagicCande = 0;
	public static ArrayList<String> stockName;
	ChromeOptions option;
	Row row;
	Date todayDate;

	public static void main(String args[]) throws IOException, InvalidFormatException, InterruptedException {
		SelectedScript2 script = new SelectedScript2();
		stockName = new ArrayList<String>();
		// script.getToken();
		script.getIntrumentID();
	}

	public void getToken() throws InterruptedException, IOException {

		fis = new FileInputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
		workbook = new XSSFWorkbook(fis);
		sheetSymbolList = workbook.getSheet("symbol_indicators");
		rowSymbol = sheetSymbolList.getLastRowNum();

		WebDriver wd = new ChromeDriver();
		WebDriverWait wc = new WebDriverWait(wd, 60);

		wd.get("https://www.nseindia.com/live_market/dynaContent/live_watch/pre_open_market/pre_open_market.htm");
		Thread.sleep(10000);

		List<WebElement> myElements = wd.findElements(By.xpath("//table[@id='preOpenNiftyTab']/tbody/tr"));

		for (int i = 3; i < myElements.size(); i++) {
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
		System.out.append(localStorage.getItem("__storejs_kite_public_token").replace("\"", ""));

		kf_session = wd.manage().getCookieNamed("kf_session").getValue();

		for (int i = 0; i < stockName.size(); i++) {
			System.out.append(stockName.get(i));
			reponseOfInstrumentToken = given()
					.headers("content-type", "application/x-www-form-urlencoded", "accept", "application/json",
							"cookie",
							"__cfduid=d21217c0877ef1707cd1b611d605c488c1555093243;kf_session=" + kf_session + "",
							"x-csrftoken", localStorage.getItem("__storejs_kite_public_token").replace("\"", ""))
					.formParam("segment", "NSE").formParam("tradingsymbol", stockName.get(i))
					.formParam("watch_id", "2021834").formParam("weight", "1")
					.post("https://kite.zerodha.com/api/marketwatch/2021834/items").then().extract().response();
			System.out.println(reponseOfInstrumentToken.asString());
			JsonObject obj = (JsonObject) new JsonParser().parse(reponseOfInstrumentToken.asString());
			JsonObject data = (JsonObject) obj.get("data");
			row = sheetSymbolList.createRow(++rowSymbol);
			row.createCell(0).setCellValue(formatter.format(date));
			row.createCell(1).setCellValue(stockName.get(i));
			row.createCell(2).setCellValue(data.get("instrument_token").getAsString());
			fis.close();
			FileOutputStream fos = new FileOutputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
			workbook.write(fos);
			fos.close();
		}

	}

	public void getIntrumentID() throws IOException, InvalidFormatException {

		LocalDate currentDate = LocalDate.now();
		int dom = currentDate.getDayOfMonth();
		Month m = currentDate.getMonth();
		JsonObject objOptionPE, objOptionCE, dataOptionPE, dataOptionCE;

		fis = new FileInputStream(new File("./src/test/resources/symbol_indicators.xlsx"));
		workbook = new XSSFWorkbook(fis);
		sheetSymbolList = workbook.getSheet("symbol_indicators");
		rowSymbol = sheetSymbolList.getLastRowNum();
		Row rows;

		double prvhigh, prvlow, prvopen, prvclose, prshigh, prslow, prsopen, prsclose;

		for (int row = 1; row <= rowSymbol; row++) {
			int j = 0;
			Response response = given().get("https://kitecharts-aws.zerodha.com/api/chart/"
					+ sheetSymbolList.getRow(row).getCell(2).getStringCellValue()
					+ "/15minute?public_token=KMq6sH0j6uCjyoInewjh70ciErD2yR4k&user_id=YG7487&api_key=kitefront&access_token=&from=2019-04-16&to=2019-04-16&ciqrandom=1555095963813")
					.then().extract().response();

			JsonObject obj = (JsonObject) new JsonParser().parse(response.asString());
			JsonObject data = (JsonObject) obj.get("data");
			JsonArray candles = (JsonArray) data.get("candles");

			for (int i = 0; i < candles.size(); i++) {
				j++;
				if (j == candles.size()) {
					break;
				}
				JsonArray previousCandle = (JsonArray) candles.get(i);
				JsonArray presentCandle = (JsonArray) candles.get(j);

				prvopen = previousCandle.get(1).getAsDouble();
				prvhigh = previousCandle.get(2).getAsDouble();
				prvclose = previousCandle.get(4).getAsDouble();
				prvlow = previousCandle.get(3).getAsDouble();

				prsopen = presentCandle.get(1).getAsDouble();
				prshigh = presentCandle.get(2).getAsDouble();
				prsclose = presentCandle.get(4).getAsDouble();
				prslow = presentCandle.get(3).getAsDouble();

				if (prvhigh > prshigh && prvlow < prslow) {

					if (prvclose > prvopen && prsclose < prsopen || prvclose < prvopen && prsclose > prsopen) {

						if (prvhigh - prslow < prvhigh * 0.01) {
							sheetMagicCandleList = workbook.getSheet("MagicCandle");
							int lastRow = sheetMagicCandleList.getLastRowNum();
							rows = sheetMagicCandleList.createRow(++lastRow);
							rows.createCell(0).setCellValue(presentCandle.get(0).getAsString());
							rows.createCell(1)
									.setCellValue(sheetSymbolList.getRow(row).getCell(1).getStringCellValue());
							rows.createCell(2)
									.setCellValue(sheetSymbolList.getRow(row).getCell(2).getStringCellValue());
							rows.createCell(3).setCellValue(prvhigh);
							rows.createCell(4).setCellValue(prvlow);
							rows.createCell(5).setCellValue(prshigh);
							rows.createCell(6).setCellValue(prslow);

							int rounded = 0;
							int[] lt500 = { 5, 10, 20 };

							for (int div : lt500) {

								rounded = ((int) prvhigh / div) * div;
								String pe = sheetSymbolList.getRow(row).getCell(1).getStringCellValue() + "19"
										+ m.toString().substring(0, 3).toUpperCase() + rounded + "PE";
								Response optionPE = given().headers("accept", "application/json, text/plain",
										"content-type", "application/x-www-form-urlencoded", "x-csrftoken",
										"KMq6sH0j6uCjyoInewjh70ciErD2yR4k", "cookie",
										"__cfduid=d67619e55e581a30a763a04cc831f8a7c1555502272; kf_session=WAoG3nwvRYfAsnX4CCqoNUVXvny1LQex")
										.formParam("segment", "NFO-OPT").formParam("tradingsymbol", pe)
										.formParam("watch_id", "2021836").formParam("weight", "1")
										.post("https://kite.zerodha.com/api/marketwatch/2021836/items").then().extract()
										.response();
								objOptionPE = (JsonObject) new JsonParser().parse(optionPE.asString());
								try {
									dataOptionPE = (JsonObject) objOptionPE.get("data");
									rows.createCell(8).setCellValue(dataOptionPE.get("instrument_token").getAsString());
									break;
								} catch (Exception e) {
									// TODO Auto-generated catch block
									continue;
								}

							}

							for (int div : lt500) {
								rounded = ((int) prvhigh / div) * div;
								String ce = sheetSymbolList.getRow(row).getCell(1).getStringCellValue() + "19"
										+ m.toString().substring(0, 3).toUpperCase() + rounded + "CE";
								Response optionCE = given().headers("accept", "application/json, text/plain",
										"content-type", "application/x-www-form-urlencoded", "x-csrftoken",
										"KMq6sH0j6uCjyoInewjh70ciErD2yR4k", "cookie",
										"__cfduid=d67619e55e581a30a763a04cc831f8a7c1555502272; kf_session=WAoG3nwvRYfAsnX4CCqoNUVXvny1LQex")
										.formParam("segment", "NFO-OPT").formParam("tradingsymbol", ce)
										.formParam("watch_id", "2021836").formParam("weight", "1").when()
										.post("https://kite.zerodha.com/api/marketwatch/2021836/items").then().extract()
										.response();

								objOptionCE = (JsonObject) new JsonParser().parse(optionCE.asString());
								try {
									dataOptionCE = (JsonObject) objOptionCE.get("data");
									rows.createCell(7).setCellValue(dataOptionCE.get("instrument_token").getAsString());
									break;

								} catch (Exception e) {
									continue;
								}
							}

							fis.close();
							FileOutputStream fos = new FileOutputStream(
									new File("./src/test/resources/symbol_indicators.xlsx"));
							workbook.write(fos);
							fos.close();
							break;

						}

					}
				}
				if (j >= 16) {
					break;
				}

			}

		}

	}

}
