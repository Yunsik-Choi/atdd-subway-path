package nextstep.subway.infra;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.Path;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.dto.PathDto;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.springframework.stereotype.Component;

@Component
public class DijkstraShortestPathImpl implements Path {

    private DijkstraShortestPath<Station, DefaultWeightedEdge> path;

    @Override
    public void init(final List<Line> lines) {
        WeightedMultigraph<Station, DefaultWeightedEdge> graph = new WeightedMultigraph(DefaultWeightedEdge.class);

        lines.stream()
                .peek(line -> line.getStations().forEach(graph::addVertex))
                .map(Line::getSections)
                .flatMap(Collection::stream)
                .forEach(section -> setEdge(graph, section));

        this.path = new DijkstraShortestPath(graph);
    }

    private void setEdge(final WeightedMultigraph<Station, DefaultWeightedEdge> graph, final Section section) {
        graph.setEdgeWeight(
                graph.addEdge(section.getUpStation(), section.getDownStation()),
                section.getDistance().value()
        );
    }

    @Override
    public PathDto find(final Station source, final Station target) {
        validateSourceAndTargetIsNotEqual(source, target);
        return Optional.ofNullable(this.path.getPath(source, target))
                .map(this::createGraphPathDto)
                .orElseThrow(IllegalArgumentException::new);
    }

    private void validateSourceAndTargetIsNotEqual(final Station source, final Station target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("출발역과 도착역은 같을 수 없습니다.");
        }
    }

    private PathDto createGraphPathDto(final GraphPath<Station, DefaultWeightedEdge> graphPath) {
        return new PathDto(graphPath.getVertexList(), graphPath.getWeight());
    }
}
