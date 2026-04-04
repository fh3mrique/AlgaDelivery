package com.algaworks.algadelivery.delivery.tracking.domain.model;

import com.algaworks.algadelivery.delivery.tracking.domain.exception.DomainException;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
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
    private OffsetDateTime fulfilledAt;


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

    public void editPreparationDetails(PreparationDetails details){
        verifyIfCanBeEdited();

        setSender(details.sender);
        setRecipiend(details.recipient);
        setDistanceFee(details.distanceFee);
        setCourierPayout(details.courierPayout);

        setExpectedDeliveryAt(OffsetDateTime.now().plus(details.expectedDeliveryTime));
        setTotalCost(this.getDistanceFee().add(this.getCourierPayout()));
    }

    public void place (){
        verifyIfCanBePlaced();
        this.setStatus(DeliveryStatus.WAITING_FOR_COURIER);
        this.setPlacedAt(OffsetDateTime.now());
    }

    public void pickup(UUID courierId){
        this.setCourierId(courierId);
        this.setStatus(DeliveryStatus.IN_TRANSIT);
        this.setAssignedAt(OffsetDateTime.now());
    }

    public void markAsDelivered(){
        this.setStatus(DeliveryStatus.DELIVERY);
        this.setFulfilledAt(OffsetDateTime.now());
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

   private void verifyIfCanBePlaced(){

        if (!isFilled()){
            throw new DomainException();
        }

        if (!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
   }

   private void verifyIfCanBeEdited(){
        if (!getStatus().equals(DeliveryStatus.DRAFT)){
            throw new DomainException();
        }
   }

   private boolean isFilled(){
        return this.getSender() != null
                && this.getRecipiend() != null
                && this.getTotalCost() !=null;
   }



   @Getter
   @Setter
   @Builder
   public static class PreparationDetails{
       private ContactPoint sender;
       private ContactPoint recipient;
       private BigDecimal distanceFee;
       private BigDecimal courierPayout;
       private Duration expectedDeliveryTime;

   }

}
