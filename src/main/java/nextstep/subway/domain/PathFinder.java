package nextstep.subway.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import nextstep.subway.domain.exception.PathFindException;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

public class PathFinder {

    private final DijkstraShortestPath path;

    public PathFinder(final List<Line> lines) {
        WeightedMultigraph<Station, DefaultWeightedEdge> graph = new WeightedMultigraph(DefaultWeightedEdge.class);
        initGraph(lines, graph);
        this.path = new DijkstraShortestPath(graph);
    }

    private void initGraph(
            final List<Line> lines,
            final WeightedMultigraph<Station, DefaultWeightedEdge> graph
    ) {
        lines.stream()
                .peek(line -> line.getStations().forEach(graph::addVertex))
                .map(Line::getSections)
                .flatMap(Collection::stream)
                .forEach(section -> setEdge(graph, section));
    }

    private void setEdge(final WeightedMultigraph<Station, DefaultWeightedEdge> graph, final Section section) {
        graph.setEdgeWeight(
                graph.addEdge(section.getUpStation(), section.getDownStation()),
                section.getDistance().value()
        );
    }

    public GraphPath find(final Station source, final Station target) {
        try {
            validateSourceAndTargetIsNotEqual(source, target);
            return Optional.ofNullable(this.path.getPath(source, target))
                    .orElseThrow(PathFindException::new);
        } catch (IllegalArgumentException e) {
            throw new PathFindException();
        }
    }

    private void validateSourceAndTargetIsNotEqual(final Station source, final Station target) {
        if (source.equals(target)) {
            throw new IllegalArgumentException("출발역과 도착역은 같을 수 없습니다.");
        }
    }
}
