package com.dodam.rent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="rent")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class RentEntity {
	
}
