package com.algaworks.algadelivery.delivery.tracking.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Setter(AccessLevel.PRIVATE)
@Getter
public class Delivery {

    @EqualsAndHashCode.Include
    private UUID id;

    private UUID courierId;

    private DeliveryStatus status;

    private OffsetDateTime placedAt;
    private OffsetDateTime assignedAt;
    private OffsetDateTime expectedDeliveryAt;

    private BigDecimal distanceFee;
    private BigDecimal courierPayout;
    private BigDecimal totalCost;


    private Integer totalItems;

    private ContactPoint sender;
    private ContactPoint recipiend;

    private List<Item> items = new ArrayList<>();


    //Factoty pra instancia um encomenda com o status inicial
    public static Delivery draft(){
        Delivery delivery = new Delivery();
        delivery.setId(UUID.randomUUID());
        delivery.setStatus(DeliveryStatus.DRAFT);
        delivery.setTotalItems(0);
        delivery.setTotalCost(BigDecimal.ZERO);
        delivery.setCourierPayout(BigDecimal.ZERO);
        delivery.setDistanceFee(BigDecimal.ZERO);
        return delivery;
    }

    public UUID addItem(String name, int quantity){
        Item item = Item.brandNew(name, quantity);
        items.add(item);
        caculateTotalItems();
        return item.getId();
    }

    public void removeItem(UUID itemId){
        items.removeIf(item -> item.getId().equals(itemId));
        caculateTotalItems();
    }

    public void changeItemQuantity(UUID itemId, int quantity ){
        Item item= getItems().stream().filter(i -> i.getId().equals(itemId))
                .findFirst().orElseThrow();

        item.setQuantity(quantity);
        caculateTotalItems();
    }

    public void removeItems(){
        items.clear();
        caculateTotalItems();
    }


    // Apenas o Aggregate Root (Delivery) pode modificar a lista de itens.
    // A lista é exposta como somente leitura para garantir encapsulamento
    // e manter a consistência das regras de negócio.
    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }


    private void caculateTotalItems(){
        int totalItems = getItems().stream().mapToInt(Item::getQuantity).sum();

         setTotalItems(totalItems);
    }

}
