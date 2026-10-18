package com.geli.warehouse.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false, unique = true)
    private ItemVariant variant;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reorderThreshold = 5;

    @Version
    private Long version;

    private Instant lastRestockedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public int getAvailableQuantity() {
        return Math.max(0, quantity - reservedQuantity);
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }

    public boolean isLowStock() {
        return getAvailableQuantity() <= reorderThreshold;
    }
}
