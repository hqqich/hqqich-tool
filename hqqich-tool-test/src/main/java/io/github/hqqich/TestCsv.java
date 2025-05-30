package io.github.hqqich;

import io.github.hqqich.csv.entity.CsvProperty;

/**
 * Created by ChenHao on 2023/10/19 is 10:59.
 *
 * @author hqqich
 */

public class TestCsv {

	@CsvProperty("a")
	private String code;

	@CsvProperty("b")
	private String speed;

	@CsvProperty("c")
	private String power;

	@CsvProperty("d")
	private String powerReal;

	public TestCsv() {
	}

	public TestCsv(String code, String speed, String power, String powerReal) {
		this.code = code;
		this.speed = speed;
		this.power = power;
		this.powerReal = powerReal;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}

	public String getPower() {
		return power;
	}

	public void setPower(String power) {
		this.power = power;
	}

	public String getPowerReal() {
		return powerReal;
	}

	public void setPowerReal(String powerReal) {
		this.powerReal = powerReal;
	}

	@Override
	public String toString() {
		return "TestCsv{" +
				"code='" + code + '\'' +
				", speed='" + speed + '\'' +
				", power='" + power + '\'' +
				", powerReal='" + powerReal + '\'' +
				'}';
	}
}
