package com.robin.book.springboot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robin.book.springboot.domain.posts.Posts;
import com.robin.book.springboot.domain.posts.PostsRepository;
import com.robin.book.springboot.service.posts.PostsService;
import com.robin.book.springboot.web.dto.PostsSaveRequestDto;

import com.robin.book.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private PostsService postsService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Before
    public void setup(){ //매번 테스트가 시작되기 전에 MockMvc 인스턴스를 생성한다.
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @After
    public void tearDown() throws Exception{
        postsRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles="USER")
    public void PostsSave() throws Exception{
        //given
        String title="title";
        String content="content";

        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder()
                .title(title)
                .content(content)
                .author("author")
                .build();
        String url="http://localhost:"+port+"/api/v1/posts";


        //when
        mvc.perform(post(url) //생성된 MockMvc를 통해 API를 테스트한다.
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isOk());

        //then
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);
        /*
        //when
        ResponseEntity<Long> responseEntity = restTemplate
                .postForEntity(url,requestDto,Long.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);

         */
    }

    @Test
    @WithMockUser(roles="USER")
    public void DtoSaveContent(){
        //given
        PostsSaveRequestDto dto = PostsSaveRequestDto.builder()
                .author("jjj@gmail.com")
                .content("테스트")
                .title("테스트 타이틀")
                .build();


        //when
        postsService.save(dto); //PostsApiController - save테스팅 통과함

        //then
        Posts posts = postsRepository.findAll().get(0);
        assertThat(posts.getAuthor()).isEqualTo(dto.getAuthor());
        assertThat(posts.getContent()).isEqualTo(dto.getContent());
        assertThat(posts.getTitle()).isEqualTo(dto.getTitle());
    }

    @Test
    @WithMockUser(roles="USER")
    public void PostsUpdate() throws Exception{
        //given
        Posts savedPosts = postsRepository.save(Posts.builder()
                .title("title")
                .content("content")
                .author("author")
                .build());
        Long updateId = savedPosts.getId();
        String expectedTitle = "title2";
        String expectedContend = "content2";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder().title(expectedTitle).content(expectedContend).build();

        String url = "http://localhost:"+port+"/api/v1/posts/"+updateId;

        HttpEntity<PostsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

        //when
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isOk());

        //then
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContend);

        /*
        //when
        ResponseEntity<Long> responseEntity = restTemplate
                .exchange(url, HttpMethod.PUT, requestEntity, Long.class);

        //then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);
        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContend);

         */
    }

    @Test
    public void PostsDelete() throws Exception{
        //given
        Posts posts = postsRepository.save(Posts.builder()
                .title("title")
                .content("content")
                .author("author")
                .build());
        Long deleteId = posts.getId();

        String url = "http://localhost:"+port+"/api/v1/posts/"+deleteId;

        //when
        restTemplate.delete(url);

    }
}
