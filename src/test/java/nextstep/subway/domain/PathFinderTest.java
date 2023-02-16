package nextstep.subway.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import nextstep.subway.domain.dto.PathDto;
import nextstep.subway.domain.exception.PathFindException;
import nextstep.subway.infra.DijkstraShortestPathImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("경로 관련 기능")
class PathFinderTest {

    private Line 삼호선;
    private Line 이호선;
    private Line 신분당선;
    private Line 수인분당선;

    private Station 남부터미널역;
    private Station 교대역;
    private Station 강남역;
    private Station 양재역;
    private Station 정자역;

    @BeforeEach
    void setUp() {
        삼호선 = new Line("3호선", "bg-orange-500");
        이호선 = new Line("2호선", "bg-green-500");
        신분당선 = new Line("신분당선", "bg-red-500");
        수인분당선 = new Line("수인분당선", "bg-yellow-500");

        남부터미널역 = new Station("남부터미널역");
        교대역 = new Station("교대역");
        강남역 = new Station("강남역");
        양재역 = new Station("양재역");
        정자역 = new Station("정자역");
    }

    /**
     * 교대역   --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역 --- *3호선* --- 양재역  --- *수인분당선* ---  정자역
     */
    @DisplayName("시작역과 도착역을 기준으로 최단경로를 반환한다.")
    @Test
    void find() {
        삼호선.addSection(new Section(삼호선, 교대역, 남부터미널역, 1));
        삼호선.addSection(new Section(삼호선, 남부터미널역, 양재역, 100));
        이호선.addSection(new Section(이호선, 교대역, 강남역, 4));
        신분당선.addSection(new Section(신분당선, 강남역, 양재역, 5));
        수인분당선.addSection(new Section(수인분당선, 양재역, 정자역, 9));
        PathFinder pathFinder = new PathFinder(new DijkstraShortestPathImpl());
        pathFinder.init(List.of(삼호선, 이호선, 신분당선, 수인분당선));
        Double expected = 19.0;

        PathDto graphPath = pathFinder.find(남부터미널역, 정자역);
        assertAll(
                () -> assertThat(graphPath.getWeight()).isEqualTo(expected),
                () -> assertThat(graphPath.getNodes()).containsExactly(남부터미널역, 교대역, 강남역, 양재역, 정자역)
        );
    }

    /**
     * 교대역
     * |
     * *3호선*
     * |
     * 남부터미널역      정자역
     */
    @DisplayName("연결되지 않은 역을 기준으로 경로를 찾을 경우 에러 처리한다.")
    @Test
    void findNotLinked() {
        삼호선.addSection(new Section(삼호선, 교대역, 남부터미널역, 1));
        PathFinder pathFinder = new PathFinder(new DijkstraShortestPathImpl());
        pathFinder.init(List.of(삼호선));

        assertThatThrownBy(() -> pathFinder.find(남부터미널역, 정자역)).isInstanceOf(PathFindException.class);
    }


    /**
     * 남부터미널역 --- *3호선* --- 교대역
     */
    @DisplayName("출발역과 도착역이 같을 경우 에러 처리한다.")
    @Test
    void findSourceAndTargetIsEqual() {
        삼호선.addSection(new Section(삼호선, 교대역, 남부터미널역, 1));
        PathFinder pathFinder = new PathFinder(new DijkstraShortestPathImpl());
        pathFinder.init(List.of(삼호선));

        assertThatThrownBy(() -> pathFinder.find(교대역, 교대역)).isInstanceOf(PathFindException.class);
    }

    /**
     * 남부터미널역 --- *3호선* --- 교대역
     */
    @DisplayName("존재하지 않는 출발역으로 경로를 찾는 경우 에러 처리한다.")
    @Test
    void findSourceIsNotExists() {
        삼호선.addSection(new Section(삼호선, 교대역, 남부터미널역, 1));
        PathFinder pathFinder = new PathFinder(new DijkstraShortestPathImpl());
        pathFinder.init(List.of(삼호선));

        assertThatThrownBy(() -> pathFinder.find(정자역, 교대역)).isInstanceOf(PathFindException.class);
    }


    /**
     * 남부터미널역 --- *3호선* --- 교대역
     */
    @DisplayName("존재하지 않는 도착역으로 경로를 찾는 경우 에러 처리한다.")
    @Test
    void findTargetIsNotExists() {
        삼호선.addSection(new Section(삼호선, 교대역, 남부터미널역, 1));
        PathFinder pathFinder = new PathFinder(new DijkstraShortestPathImpl());
        pathFinder.init(List.of(삼호선));

        assertThatThrownBy(() -> pathFinder.find(교대역, 정자역)).isInstanceOf(PathFindException.class);
    }
}
