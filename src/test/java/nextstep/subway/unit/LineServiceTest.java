package nextstep.subway.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import nextstep.subway.applicaion.LineService;
import nextstep.subway.applicaion.dto.SectionRequest;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.LineRepository;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("구간 서비스 단위 테스트")
@SpringBootTest
@Transactional
public class LineServiceTest {
    @Autowired
    private StationRepository stationRepository;
    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private LineService lineService;

    private int distance;
    private Line line;
    private Station upStation;
    private Station downStation;
    private Station deleteStation;

    @BeforeEach
    void setUp() {
        this.distance = 10;
        this.line = new Line("2호선", "bg-red-500");
        this.upStation = new Station("강남역");
        this.downStation = new Station("역삼역");
        this.deleteStation = new Station("선릉역");
    }

    @DisplayName("노선에 구간을 추가한다.")
    @Test
    void addSection() {
        // given
        // stationRepository와 lineRepository를 활용하여 초기값 셋팅
        stationRepository.save(upStation);
        stationRepository.save(downStation);
        lineRepository.save(line);

        // when
        // lineService.addSection 호출
        lineService.addSection(line.getId(), new SectionRequest(upStation.getId(), downStation.getId(), distance));

        // then
        // line.getSections 메서드를 통해 검증
        Section section = line.getSections().get(0);
        assertAll(
                () -> assertThat(line.getSections()).hasSize(1),
                () -> assertThat(section.getUpStation()).isEqualTo(upStation),
                () -> assertThat(section.getDownStation()).isEqualTo(downStation),
                () -> assertThat(section.getDistance()).isEqualTo(distance)
        );
    }

    @DisplayName("노선에 구간을 제거한다.")
    @Test
    void deleteSection() {
        // given
        // stationRepository와 lineRepository를 활용하여 초기값 셋팅
        stationRepository.save(upStation);
        stationRepository.save(downStation);
        stationRepository.save(deleteStation);
        lineRepository.save(line);
        lineService.addSection(line.getId(), new SectionRequest(upStation.getId(), downStation.getId(), distance));
        lineService.addSection(line.getId(), new SectionRequest(downStation.getId(), deleteStation.getId(), distance));

        // when
        // lineService.deleteSection 호출
        lineService.deleteSection(line.getId(), deleteStation.getId());

        // then
        // line.getSections 메서드를 통해 검증
        List<Section> sections = line.getSections();
        assertAll(
                () -> assertThat(sections).hasSize(1),
                () -> assertThat(sections.get(0).getUpStation()).isEqualTo(upStation),
                () -> assertThat(sections.get(0).getDownStation()).isEqualTo(downStation),
                () -> assertThat(sections.get(0).getDistance()).isEqualTo(distance)
        );
    }
}
