package com.robin.book.springboot.web.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloResponseDtoTest {

    @Test
    public void lombokTest(){
        //given
        String name="test";
        int amount = 100;

        //when
        HelloResponseDto dto = new HelloResponseDto("test",100);

        //then
        assertThat(dto.getName()).isEqualTo(name);
        assertThat(dto.getAmount()).isEqualTo(amount);
    }
}
