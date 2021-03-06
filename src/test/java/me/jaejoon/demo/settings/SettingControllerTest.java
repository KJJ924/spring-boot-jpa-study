package me.jaejoon.demo.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.jaejoon.demo.WithAccount;
import me.jaejoon.demo.account.AccountRepository;
import me.jaejoon.demo.account.AccountService;
import me.jaejoon.demo.domain.Account;
import me.jaejoon.demo.domain.Tag;
import me.jaejoon.demo.domain.Zone;
import me.jaejoon.demo.form.SignUpForm;
import me.jaejoon.demo.form.TagForm;
import me.jaejoon.demo.form.ZoneForm;
import me.jaejoon.demo.tag.TagRepository;
import me.jaejoon.demo.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static me.jaejoon.demo.settings.SettingController.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
        tagRepository.deleteAll();
    }
    @Test
    @WithAccount("jaejoon")
    @DisplayName("관심지역 수정 폼")
    void zonesForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ZONES))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));

    }
    @Test
    @WithAccount("jaejoon")
    @DisplayName("관심지역 추가")
    void addZones() throws Exception {
        Zone testZone = Zone.builder().city("Asan").localNameOfCity("아산시").province("South Chungcheong").build();

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT+SETTINGS+ZONES+"/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm)))
                .andExpect(status().isOk());

        Account jaejoon = accountRepository.findByNickname("jaejoon");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());

        assertThat(jaejoon.getZones().contains(zone)).isTrue();
    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("관심지역 삭제")
    void removeZones() throws Exception {
        Account jaejoon = accountRepository.findByNickname("jaejoon");
        Zone testZone = Zone.builder().city("Asan").localNameOfCity("아산시").province("South Chungcheong").build();
        jaejoon.getZones().add(testZone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT+SETTINGS+ZONES+"/remove")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm)))
                .andExpect(status().isOk());

        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());

        assertThat(jaejoon.getZones().contains(zone)).isFalse();
    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("태그 수정 폼")
    void tagForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+TAGS))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whiteList"));

    }
    @Test
    @WithAccount("jaejoon")
    @DisplayName("태그 추가")
    void addTag() throws Exception {

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT+SETTINGS+TAGS +"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertThat(newTag).isNotNull();

        Account account = accountRepository.findByNickname("jaejoon");
        assertThat(account.getTags().contains(newTag)).isTrue();
    }
    @Test
    @WithAccount("jaejoon")
    @DisplayName("태그 삭제")
    void removeTag() throws Exception {
        Account account = accountRepository.findByNickname("jaejoon");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(account,newTag);

        assertThat(account.getTags().contains(newTag)).isTrue();

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT+SETTINGS+TAGS +"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertThat(account.getTags().contains(newTag)).isFalse();
    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("프로필수정_페이지_이동")
    void updateForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+PROFILE))
                .andExpect(view().name(SETTINGS+PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

    }
    @Test
    @WithAccount("jaejoon")
    @DisplayName("프로필수정_성공")
    void updateProfile() throws Exception {
        String bio = "안녕하세요 안녕하세요 ";
        mockMvc.perform(post(ROOT+SETTINGS+PROFILE)
                    .param("bio", bio)
                    .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("jaejoon");
        assertThat(account.getBio()).isEqualTo(bio);
    }


    @Test
    @WithAccount("jaejoon")
    @DisplayName("프로필수정_실패")
    void updateProfile_error() throws Exception {
        String bio = "안녕하세요 안녕하세요 안녕하세요 안녕하세요안녕하세요 안녕하세요안녕하세요 안녕하세요안녕하세요 안녕하세요";
        mockMvc.perform(post(ROOT+SETTINGS+PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());


        Account account = accountRepository.findByNickname("jaejoon");
        assertThat(account.getBio()).isNull();
    }

    @Test
    @WithAccount("jaejoon") // 기본 비밀번호 12345689
    @DisplayName("패스워드 수정 페이지 이동")
    void updatePasswordForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+PASSWORD))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PASSWORD));

    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("패스워드 변경 성공")
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT+SETTINGS+PASSWORD)
                .param("newPassword","1234567890")
                .param("newPasswordConfirm","1234567890")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS+PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account jaejoon = accountRepository.findByNickname("jaejoon");
        boolean matches = passwordEncoder.matches("1234567890", jaejoon.getPassword());
        assertThat(matches).isTrue();
    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("패스워드 변경 실패")
    void updatePassword_wrong() throws Exception {
        mockMvc.perform(post(ROOT+SETTINGS+PASSWORD)
                .param("newPassword","12323231490")
                .param("newPasswordConfirm","1234567890")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+PASSWORD))
                .andExpect(model().hasErrors());
    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("닉네임 변경 수정 페이지 이동")
    void updateNickNameForm() throws Exception {
        mockMvc.perform(get(ROOT+SETTINGS+ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ACCOUNT))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }
    @Test
    @WithAccount("jaejoon")
    @DisplayName("닉네임 변경_중복실패")
    void updateNickName_fail() throws Exception {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("중복닉네임");
        signUpForm.setEmail("test@gmail.com");
        signUpForm.setPassword("123123131313");
        accountService.processNewAccount(signUpForm);

        mockMvc.perform(post(ROOT+SETTINGS+ACCOUNT)
                .param("nickname","중복닉네임")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS+ACCOUNT))
                .andExpect(model().hasErrors());

        boolean beforeName = accountRepository.existsByNickname("jaejoon");
        assertThat(beforeName).isTrue();
    }

    @Test
    @WithAccount("jaejoon")
    @DisplayName("닉네임 변경_성공")
    void updateNickName() throws Exception {
        mockMvc.perform(post(ROOT+SETTINGS+ACCOUNT)
                .param("nickname","변경닉네임")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SETTINGS+ACCOUNT))
                .andExpect(flash().attributeExists("message"));
        boolean beforeName = accountRepository.existsByNickname("jaejoon");
        boolean afterName = accountRepository.existsByNickname("변경닉네임");

        assertThat(beforeName).isFalse();
        assertThat(afterName).isTrue();
    }

}