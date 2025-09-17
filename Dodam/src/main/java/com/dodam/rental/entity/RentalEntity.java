package com.dodam.rental.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="rental")
@Getter
@Setter
@NoArgsConstructor
@Builder
public class RentalEntity {
	
}
