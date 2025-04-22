import React from "react";
import { Sankey, sankeyCenter, SankeyNode, SankeyLink } from "@visx/sankey";
import { Group } from "@visx/group";
import { LinkHorizontal } from "@visx/shape";
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal } from "d3-scale";

type NodeDatum = { name: string };

interface LinkDatum {
    source: number;
    target: number;
    value: number;
}

const demoGraph: { nodes: NodeDatum[]; links: LinkDatum[] } = {
    nodes: [
        { name: "Výrobce" },
        { name: "Distributor A" },
        { name: "Distributor B" },
        { name: "Lékárny" }
    ],
    links: [
        { source: 0, target: 1, value: 70 },
        { source: 0, target: 2, value: 30 },
        { source: 1, target: 3, value: 55 },
        { source: 2, target: 3, value: 25 }
    ]
};

interface SankeyChartProps {
    height?: number;
}

export const SankeyChart: React.FC<SankeyChartProps> = ({ height = 500 }) => {
    const color = scaleOrdinal<string, string>()
        .domain(demoGraph.nodes.map(n => n.name))
        .range(["#34558a", "#4f6da2", "#6c88b8", "#8aa2cb"]);

    return (
        <ParentSize debounceTime={10}>
            {({ width }) => {
                if (!width) return null; // ParentSize ještě nezná šířku

                const margin = { top: 16, right: 16, bottom: 16, left: 16 };
                const innerW = width - margin.left - margin.right;
                const innerH = height - margin.top - margin.bottom;

                return (
                    <svg width={width} height={height}>
                        <Sankey<NodeDatum, LinkDatum>
                            root={demoGraph}
                            size={[innerW, innerH]}
                            nodeAlign={sankeyCenter}
                            nodeWidth={20}
                            nodePadding={16}
                        >
                            {({ graph, createPath }) => (
                                <Group top={margin.top} left={margin.left}>
                                    {graph.links.map((link, i) => (
                                        <LinkHorizontal<
                                                SankeyLink<NodeDatum, LinkDatum>,
                                                NodeDatum
                                            >
                                            key={i}
                                            data={link}
                                            path={createPath}
                                            stroke={
                                                typeof link.target === "object"
                                                    ? color((link.target as SankeyNode<NodeDatum, LinkDatum> & NodeDatum).name)
                                                    : "#999"
                                            }
                                            strokeWidth={Math.max(1, link.width ?? 1)}
                                            strokeOpacity={0.35}
                                            fill="none"
                                        />
                                    ))}

                                    {graph.nodes.map((node, i) => {
                                        const x0 = node.x0 ?? 0;
                                        const x1 = node.x1 ?? 0;
                                        const y0 = node.y0 ?? 0;
                                        const y1 = node.y1 ?? 0;
                                        const name = (node as NodeDatum).name;

                                        return (
                                            <Group key={i}>
                                                <rect
                                                    x={x0}
                                                    y={y0}
                                                    width={x1 - x0}
                                                    height={y1 - y0}
                                                    fill={color(name)}
                                                    rx={4}
                                                />
                                                <text
                                                    x={x0 < innerW / 2 ? x1 + 6 : x0 - 6}
                                                    y={y0 + (y1 - y0) / 2}
                                                    dy="0.35em"
                                                    textAnchor={x0 < innerW / 2 ? "start" : "end"}
                                                    fontSize={12}
                                                    fill="#333"
                                                >
                                                    {name}
                                                </text>
                                            </Group>
                                        );
                                    })}
                                </Group>
                            )}
                        </Sankey>
                    </svg>
                );
            }}
        </ParentSize>
    );
};
