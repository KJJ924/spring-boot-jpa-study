package me.jaejoon.demo.domain;

import lombok.*;
import me.jaejoon.demo.account.UserAccount;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedEntityGraph(name = "Study.withAll",attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("members"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("zones")
})
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")})

@NamedEntityGraph(name = "Study.Managers",attributeNodes = {
        @NamedAttributeNode("managers")})

@NamedEntityGraph(name = "Study.Members",attributeNodes = {
        @NamedAttributeNode("members")})

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@NoArgsConstructor @AllArgsConstructor @Builder
public class Study {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingUpdateDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    public void addManger(Account account) {
        this.managers.add(account);
    }

    public void addMember(Account account) {
        this.members.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);

    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public String getImage() {
        return image != null ? image : "/images/default_banner.png";
    }

    public void publish() {
        if(!this.closed && !this.published){
            this.published=true;
            this.publishedDateTime=LocalDateTime.now();
        }else {
            throw new RuntimeException("스터디를 공개할 수없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
        }
    }

    public void closed() {
        if(!this.closed && this.published){
            this.closed = true;
            this.closedDateTime = LocalDateTime.now();
        }else {
            throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
        }
    }

    public void startRecruiting() {
        if(canUpdateRecruiting()){
            this.recruiting =true;
            this.recruitingUpdateDateTime =LocalDateTime.now();
        }else {
            throw  new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤에 다시 시도하세요");
        }
    }

    public void stopRecruiting() {
        if(canUpdateRecruiting()){
            this.recruiting =false;
            this.recruitingUpdateDateTime =LocalDateTime.now();
        }else {
            throw  new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤에 다시 시도하세요");
        }
    }

    public boolean canUpdateRecruiting() {
        return this.published&&this.recruitingUpdateDateTime==null||
                this.recruitingUpdateDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }

    public boolean isRemovable() {
        return !this.published;
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }
}
