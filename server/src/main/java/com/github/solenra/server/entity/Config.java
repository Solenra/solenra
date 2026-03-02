package com.github.solenra.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CONFIG", uniqueConstraints = {@UniqueConstraint(columnNames = {"CODE"})})
public class Config {

	public static final String CODE_TERMS_OF_SERVICE_HTML = "terms-of-service-html";
	public static final String CODE_PRIVACY_POLICY_HTML = "privacy-policy-html";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "CODE", nullable = false)
	private String code;

	@Lob
	@Column(name = "SETTING_VALUE")
	private String value;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
