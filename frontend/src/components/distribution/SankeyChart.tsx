import React, {useState} from "react";
import { Sankey, sankeyCenter, SankeyNode, SankeyLink } from "@visx/sankey";
import { Group } from "@visx/group";
import { LinkHorizontal } from "@visx/shape";
import { ParentSize } from "@visx/responsive";
import { scaleOrdinal } from "d3-scale";

type NodeDatum = { id: string; label: string };
type LinkDatum = { source: string; target: string; value: number };

interface SankeyChartProps {
    nodes: NodeDatum[];
    links: LinkDatum[];
    height?: number;
}

type TooltipData = {
    x: number;
    y: number;
    content: string;
};

export const SankeyChart: React.FC<SankeyChartProps> = ({
                                                            nodes,
                                                            links,
                                                            height = 500
                                                        }) => {
    const color = scaleOrdinal<string, string>()
        .domain(nodes.map(n => n.label))
        .range(["#34558a", "#4f6da2", "#6c88b8", "#8aa2cb", "#abc", "#ddd"]);

    const [tooltip, setTooltip] = useState<TooltipData | null>(null);

    const graph = { nodes, links };

    return (
        <ParentSize debounceTime={10}>
            {({ width }) => {
                if (!width) return null;

                const margin = { top: 16, right: 16, bottom: 16, left: 16 };
                const innerW = width - margin.left - margin.right;
                const innerH = height - margin.top - margin.bottom;

                return (
                    <svg width={width} height={height}>
                        <Sankey<NodeDatum, LinkDatum>
                            root={graph}
                            size={[innerW, innerH]}
                            nodeAlign={sankeyCenter}
                            nodeWidth={20}
                            nodePadding={16}
                            nodeId={(d) => d.id}
                        >
                            {({ graph, createPath }) => (
                                <Group top={margin.top} left={margin.left}>
                                    {graph.links.map((link, i) => {
                                        const sourceLabel =
                                            typeof link.source === "object"
                                                ? (link.source as SankeyNode<NodeDatum, LinkDatum>).label
                                                : String(link.source);

                                        const targetLabel =
                                            typeof link.target === "object"
                                                ? (link.target as SankeyNode<NodeDatum, LinkDatum>).label
                                                : String(link.target);

                                        const pathD = createPath(link);
                                        if (!pathD) return null;

                                        return (
                                            <path
                                                key={`link-${i}`}
                                                d={pathD}
                                                fill="none"
                                                stroke={color(targetLabel)}
                                                strokeWidth={Math.max(1, link.width ?? 1)}
                                                strokeOpacity={0.35}
                                            >
                                                <title>{`${sourceLabel} → ${targetLabel}: ${link.value} balení`}</title>
                                            </path>
                                        );
                                    })}

                                    {graph.nodes.map((node, i) => {
                                        const x0 = node.x0 ?? 0;
                                        const x1 = node.x1 ?? 0;
                                        const y0 = node.y0 ?? 0;
                                        const y1 = node.y1 ?? 0;
                                        const label = node.label;

                                        return (
                                            <Group key={`node-${i}`}>
                                                <rect
                                                    x={x0}
                                                    y={y0}
                                                    width={x1 - x0}
                                                    height={y1 - y0}
                                                    fill={color(label)}
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
                                                    {label}
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
