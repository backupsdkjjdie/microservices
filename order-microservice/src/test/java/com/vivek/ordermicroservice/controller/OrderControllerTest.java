package com.vivek.ordermicroservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.vivek.ordermicroservice.OrderServiceConfig;
import com.vivek.ordermicroservice.entity.Order;
import com.vivek.ordermicroservice.model.OrderRequest;
import com.vivek.ordermicroservice.model.OrderResponse;
import com.vivek.ordermicroservice.model.PaymentMode;
import com.vivek.ordermicroservice.repository.OrderRepository;
import com.vivek.ordermicroservice.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest({"server.port=0"})
@EnableConfigurationProperties
@AutoConfigureMockMvc
@ContextConfiguration(classes = {OrderServiceConfig.class})
public class OrderControllerTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MockMvc mockMvc;
    @RegisterExtension
    static WireMockExtension wireMockServer =
            WireMockExtension.newInstance()
                    .options(WireMockConfiguration
                            .wireMockConfig()
                            .port(8080))
                    .build();

    private ObjectMapper objectMapper =
            new ObjectMapper()
                    .findAndRegisterModules()
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @BeforeEach
    void setup() throws IOException {
        getProductDetailResponse();
        doPayment();
        getPaymentDetails();
        reduceQuantity();
    }

    private void reduceQuantity() {
        wireMockServer.stubFor(put(urlMatching("/product/reduceQuantity/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getPaymentDetails() throws IOException {
        wireMockServer.stubFor(get(urlMatching("/payment/.*"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                StreamUtils.copyToString(
                                    OrderControllerTest.class
                                            .getClassLoader()
                                            .getResourceAsStream("mock/GetPayment.json"),
                                        Charset.defaultCharset()
                                )
                        )));
    }

    private void doPayment() {
        wireMockServer.stubFor(WireMock.post("/payment")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type",MediaType.APPLICATION_JSON_VALUE)));
    }

    private void getProductDetailResponse() throws IOException {
//        /product/1 : GET
        wireMockServer.stubFor(get("/product/1")
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(StreamUtils.copyToString(
                                OrderControllerTest.class
                                        .getClassLoader()
                                        .getResourceAsStream("mock/GetProduct.json"),
                                Charset.defaultCharset()
                        ))));
    }

    @Test
    public void test_When_Place_Order_DoPayment_Success() throws Exception {
        //first place the order
        //get the order by order id from db and check output
        OrderRequest orderRequest = getOrderRequestMock();
        MvcResult mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/order/place_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String orderId = mvcResult.getResponse().getContentAsString();
        Optional<Order> order = orderRepository.findById(Long.valueOf(orderId));
        Assertions.assertTrue(order.isPresent());
        Order o = order.get();
        Assertions.assertEquals(Long.parseLong(orderId),o.getId());
        Assertions.assertEquals("PLACED",o.getOrderStatus());
        Assertions.assertEquals(orderRequest.getTotalAmount(),o.getAmount());
        Assertions.assertEquals(orderRequest.getQuantity(), o.getQuantity());
    }

    private OrderRequest getOrderRequestMock() {
        return OrderRequest.builder()
                .productId(1)
                .paymentMode(PaymentMode.CASH)
                .quantity(10)
                .totalAmount(200)
                .build();
    }

    @Test
    public void test_WhenGetOrder_success() throws Exception {
        MvcResult mvcResult =
                mockMvc.perform(MockMvcRequestBuilders.get("/order/1")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        Order order = orderRepository.findById(1l).get();
        String expectedResponse = getOrderResponse(order);
        Assertions.assertEquals(expectedResponse,response);
    }

    private String getOrderResponse(Order order) throws IOException {
        OrderResponse.PaymentDetails paymentDetails =
                objectMapper.readValue(
                        StreamUtils.copyToString(
                                OrderControllerTest.class.getClassLoader()
                                        .getResourceAsStream("mock/GetPayment.json")
                                ,Charset.defaultCharset())
                        , OrderResponse.PaymentDetails.class
                );
        paymentDetails.setPaymentStatus("SUCCESS");

        OrderResponse.ProductDetails productDetails =
                objectMapper.readValue(
                        StreamUtils.copyToString(
                                OrderControllerTest.class.getClassLoader()
                                        .getResourceAsStream("mock/GetProduct.json")
                                ,Charset.defaultCharset())
                        , OrderResponse.ProductDetails.class
                );

        OrderResponse orderResponse =
                OrderResponse.builder()
                        .paymentDetails(paymentDetails)
                        .productDetails(productDetails)
                        .orderStatus(order.getOrderStatus())
                        .orderDate(order.getOrderDate())
                        .amount(order.getAmount())
                        .orderId(order.getId())
                        .build();

        return objectMapper.writeValueAsString(orderResponse);

    }

    @Test
    public void test_when_GetOrder_Order_Not_Found() throws Exception {
        MvcResult mvcResult =
                mockMvc.perform(MockMvcRequestBuilders.get("/order/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andExpect(MockMvcResultMatchers.status().isNotFound())
                        .andReturn();

    }
}